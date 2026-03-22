
package com.victoruk.dicestore.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victoruk.dicestore.common.config.appProperties.AppProperties;
import com.victoruk.dicestore.common.exception.OrderAlreadyPaidException;
import com.victoruk.dicestore.common.exception.PaymentInitializationException;
import com.victoruk.dicestore.common.exception.ResourceNotFoundException;
import com.victoruk.dicestore.infrastructure.email.EmailService;
import com.victoruk.dicestore.order.entity.Order;
import com.victoruk.dicestore.cart.repository.CartItemRepository;
import com.victoruk.dicestore.order.entity.OrderStatus;
import com.victoruk.dicestore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackService {

    private final AppProperties appProperties;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public InitializePaymentResponseDto initializePayment(Long orderId, String email) {
        log.info("Initializing Paystack payment for order [{}], email [{}]", orderId, email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            throw new OrderAlreadyPaidException(orderId);
        }

        // [ADDED] Check if a pending payment already exists for this order.
        // This handles retries — if the user previously got a checkout URL but
        // never completed payment, retrying would cause a duplicate entry error
        // on the payments.order_id unique constraint. Instead, we reuse the
        // existing reference and get a fresh authorization URL from Paystack.
        Optional<Payment> existingPayment = paymentRepository.findByOrder_OrderId(orderId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
            log.info("Reusing existing pending payment for order [{}]", orderId);
            String existingReference = existingPayment.get().getPaystackReference();
            return reinitializeWithPaystack(order, email, existingReference);
        }

        long amountInKobo = order.getTotalPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Map<String, Object> body = Map.of(
                "email", email,
                "amount", amountInKobo,
                "reference", generateReference(orderId),
                "metadata", Map.of("order_id", orderId)
        );

        try {
            // Delegated the WebClient call to the private callPaystack() helper
            PaystackInitializeResponse paystackResponse = callPaystack(body);

            // Persist a PENDING payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPaystackReference(paystackResponse.data().reference());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(order.getTotalPrice());
            paymentRepository.save(payment);

            log.info("Payment record created for order [{}] with reference [{}]",
                    orderId, paystackResponse.data().reference());

            return new InitializePaymentResponseDto(
                    paystackResponse.data().authorizationUrl(),
                    paystackResponse.data().reference()
            );

        } catch (OrderAlreadyPaidException | PaymentInitializationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Paystack API call failed for order [{}] after retries", orderId, e);
            throw new PaymentInitializationException("Payment service is currently unavailable. Please try again later.");
        }
    }

    // ------------------------------------------------------------------
    // Webhook Handler
    // ------------------------------------------------------------------

    @Async
    @Transactional
    public void handleWebhook(String rawPayload, String paystackSignature) {
        log.info("Received Paystack webhook");

        if (!isValidSignature(rawPayload, paystackSignature)) {
            log.warn("Invalid Paystack webhook signature — possible spoofed request");
            return;
        }

        PaystackWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawPayload, PaystackWebhookPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Paystack webhook payload", e);
            return;
        }

        log.info("Webhook event [{}] for reference [{}]", payload.event(), payload.data().reference());

        if (!"charge.success".equals(payload.event()) && !"charge.failed".equals(payload.event())) {
            log.info("Ignoring unhandled webhook event [{}]", payload.event());
            return;
        }

        String reference = payload.data().reference();

        Optional<Payment> existingPayment = paymentRepository.findByPaystackReference(reference);
        if (existingPayment.isEmpty()) {
            log.warn("No payment record found for reference [{}] — ignoring webhook", reference);
            return;
        }

        Payment payment = existingPayment.get();

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("Payment [{}] already processed with status [{}] — skipping duplicate webhook",
                    reference, payment.getStatus());
            return;
        }

        Order order = payment.getOrder();

        if ("charge.success".equals(payload.event())) {
            handleSuccess(payment, order, payload);
        } else {
            handleFailure(payment, order, payload);
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void handleSuccess(Payment payment, Order order, PaystackWebhookPayload payload) {
        log.info("Processing successful payment for order [{}]", order.getOrderId());

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(Instant.now());
        payment.setGatewayResponse(payload.data().gatewayResponse());
        payment.setPaymentMethod(payload.data().channel());
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);

        cartItemRepository.deleteByCartUser(order.getUser());
        log.info("Cart cleared for user [{}] after successful payment", order.getUser().getEmail());

        emailService.sendOrderConfirmationEmail(
                order.getUser().getEmail(),
                order.getUser().getName(),
                order
        );

        log.info("Order [{}] successfully paid and confirmed", order.getOrderId());
    }

    private void handleFailure(Payment payment, Order order, PaystackWebhookPayload payload) {
        log.warn("Payment failed for order [{}]", order.getOrderId());

        payment.setStatus(PaymentStatus.FAILED);
        payment.setGatewayResponse(payload.data().gatewayResponse());
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        log.warn("Order [{}] marked as FAILED due to payment failure", order.getOrderId());
    }

    //Handles retry scenario where a pending payment already exists.
    // Re-calls Paystack's initialize endpoint with the existing reference to
    // get a fresh authorization URL without inserting a duplicate payment record.
    private InitializePaymentResponseDto reinitializeWithPaystack(Order order, String email, String reference) {
        long amountInKobo = order.getTotalPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Map<String, Object> body = Map.of(
                "email", email,
                "amount", amountInKobo,
                "reference", reference, // reuse existing reference, not a new one
                "metadata", Map.of("order_id", order.getOrderId())
        );

        try {
            PaystackInitializeResponse paystackResponse = callPaystack(body);
            return new InitializePaymentResponseDto(
                    paystackResponse.data().authorizationUrl(),
                    paystackResponse.data().reference()
            );
        } catch (Exception e) {
            log.error("Paystack re-initialization failed for order [{}]", order.getOrderId(), e);
            throw new PaymentInitializationException("Payment service is currently unavailable. Please try again later.");
        }
    }

    // Extracted WebClient HTTP call into a reusable private helper.
    // Used by both the initial payment flow and the re-initialization flow,
    // keeping auth headers, timeout, and retry config in one place.
    private PaystackInitializeResponse callPaystack(Map<String, Object> body) {
        PaystackInitializeResponse paystackResponse = webClient.post()
                .uri(appProperties.paystack().initializeUrl())
                .headers(h -> h.setBearerAuth(appProperties.paystack().secretKey()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaystackInitializeResponse.class)
                .timeout(Duration.ofSeconds(5))
                .retry(1)
                .block();

        if (paystackResponse == null || !paystackResponse.status()) {
            throw new PaymentInitializationException("Payment initialization failed. Please try again.");
        }
        return paystackResponse;
    }

    private boolean isValidSignature(String rawPayload, String paystackHeader) {
        if (paystackHeader == null || rawPayload == null) return false;

        try {
            byte[] secretBytes = appProperties.paystack().webhookSecret().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA512");

            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(keySpec);

            byte[] resultBytes = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = HexFormat.of().formatHex(resultBytes);

            return MessageDigest.isEqual(
                    computedSignature.getBytes(StandardCharsets.UTF_8),
                    paystackHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Security Alert: Possible tampered webhook or config error", e);
            return false;
        }
    }

    private String generateReference(Long orderId) {
        return "DICE-" + orderId + "-" + Instant.now().toEpochMilli();
    }
}

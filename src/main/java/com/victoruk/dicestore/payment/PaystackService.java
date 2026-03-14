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
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private final RestTemplate restTemplate;

    // ------------------------------------------------------------------
    // Initialize Payment
    // ------------------------------------------------------------------

    public InitializePaymentResponseDto initializePayment(Long orderId, String email) {
        log.info("Initializing Paystack payment for order [{}], email [{}]", orderId, email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));

        // Guard: don't re-initialize a paid order
        if (order.getOrderStatus() == OrderStatus.PAID) {
            throw new OrderAlreadyPaidException(orderId);
        }

        // Amount in kobo (Paystack requires smallest currency unit)
        long amountInKobo = order.getTotalPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(appProperties.paystack().secretKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "email", email,
                "amount", amountInKobo,
                "reference", generateReference(orderId),
                "metadata", Map.of("order_id", orderId)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<PaystackInitializeResponse> response = restTemplate.postForEntity(
                    appProperties.paystack().initializeUrl(),
                    request,
                    PaystackInitializeResponse.class
            );

            PaystackInitializeResponse paystackResponse = response.getBody();

            if (paystackResponse == null || !paystackResponse.status()) {
                log.error("Paystack initialization failed for order [{}]: {}", orderId,
                        paystackResponse != null ? paystackResponse.message() : "null response");
                throw new PaymentInitializationException("Payment initialization failed. Please try again.");
            }

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
            // Let our own exceptions propagate cleanly — don't wrap them
            throw e;
        } catch (Exception e) {
            log.error("Paystack API call failed for order [{}]", orderId, e);
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

        // Step 1: Verify signature
        if (!isValidSignature(rawPayload, paystackSignature)) {
            log.warn("Invalid Paystack webhook signature — possible spoofed request");
            return;
        }

        // Step 2: Parse payload
        PaystackWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawPayload, PaystackWebhookPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Paystack webhook payload", e);
            return;
        }

        log.info("Webhook event [{}] for reference [{}]", payload.event(), payload.data().reference());

        // Step 3: Only handle charge.success and charge.failed
        if (!"charge.success".equals(payload.event()) && !"charge.failed".equals(payload.event())) {
            log.info("Ignoring unhandled webhook event [{}]", payload.event());
            return;
        }

        String reference = payload.data().reference();

        // Step 4: Idempotency check — find existing payment record
        Optional<Payment> existingPayment = paymentRepository.findByPaystackReference(reference);
        if (existingPayment.isEmpty()) {
            log.warn("No payment record found for reference [{}] — ignoring webhook", reference);
            return;
        }

        Payment payment = existingPayment.get();

        // Step 5: Skip if already processed (idempotency guard)
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

        // Clear cart only after confirmed payment
        cartItemRepository.deleteByCartUser(order.getUser());
        log.info("Cart cleared for user [{}] after successful payment", order.getUser().getEmail());

        // Send confirmation email asynchronously
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

    private boolean isValidSignature(String rawPayload, String paystackSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    appProperties.paystack().webhookSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = HexFormat.of().formatHex(hash);
            return computedSignature.equals(paystackSignature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    private String generateReference(Long orderId) {
        return "DICE-" + orderId + "-" + System.currentTimeMillis();
    }
}
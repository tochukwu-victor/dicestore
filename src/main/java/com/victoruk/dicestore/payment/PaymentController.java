package com.victoruk.dicestore.payment;


import com.victoruk.dicestore.common.security.AuthenticatedUserResolver;
import com.victoruk.dicestore.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Paystack payment initialization and webhook handling")
public class PaymentController {

    private final PaystackService paystackService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @Operation(
            summary = "Initialize payment for an order",
            description = "Calls Paystack to generate a payment URL for the given order. " +
                    "The frontend should redirect the user to the returned paymentUrl."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment URL generated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Order already paid or invalid state"),
            @ApiResponse(responseCode = "503", description = "Paystack service unavailable")
    })
    @PostMapping("/initialize/{orderId}")
    public ResponseEntity<InitializePaymentResponseDto> initializePayment(
            @Parameter(description = "ID of the order to pay for", required = true)
            @PathVariable Long orderId) {

        User user = authenticatedUserResolver.getAuthenticatedUser();
        log.info("Payment initialization request for order [{}] by user [{}]", orderId, user.getEmail());

        InitializePaymentResponseDto response = paystackService.initializePayment(orderId, user.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Paystack webhook receiver",
            description = "Receives async payment event notifications from Paystack. " +
                    "Verifies the HMAC-SHA512 signature before processing. " +
                    "Always returns 200 to prevent Paystack retry storms."
    )
    @ApiResponse(responseCode = "200", description = "Webhook received")
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("x-paystack-signature") String paystackSignature,
            @RequestBody String rawPayload) {

        log.info("Paystack webhook received");
        // Always return 200 immediately — processing is async
        paystackService.handleWebhook(rawPayload, paystackSignature);
        return ResponseEntity.ok().build();
    }
}
package com.victoruk.dicestore.payment;

public record InitializePaymentResponseDto(
        String paymentUrl,
        String reference
) {}
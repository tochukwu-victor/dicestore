package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.PaymentIntentRequestDto;
import com.victoruk.dicestore.dto.PaymentIntentResponseDto;

public interface IPaymentService {

    PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto paymentIntentRequestDto);
}

package com.victoruk.dicestore.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.victoruk.dicestore.dto.PaymentIntentRequestDto;
import com.victoruk.dicestore.dto.PaymentIntentResponseDto;
import com.victoruk.dicestore.service.IPaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements IPaymentService {


    @Override
    public PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto paymentIntentRequestDto) {

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentIntentRequestDto.amount())
                    .setCurrency(paymentIntentRequestDto.currency())
                    .addPaymentMethodType("card").build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return new PaymentIntentResponseDto(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }
}

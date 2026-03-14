package com.victoruk.dicestore.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaystackWebhookPayload(
        String event,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            @JsonProperty("reference") String reference,
            @JsonProperty("status") String status,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("channel") String channel,
            @JsonProperty("paid_at") String paidAt,
            @JsonProperty("gateway_response") String gatewayResponse
    ) {}
}
package com.victoruk.dicestore.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaystackInitializeResponse(
        boolean status,
        String message,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            @JsonProperty("authorization_url") String authorizationUrl,
            @JsonProperty("access_code") String accessCode,
            @JsonProperty("reference")
            String reference
    ) {}
}
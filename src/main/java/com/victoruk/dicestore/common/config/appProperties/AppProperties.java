package com.victoruk.dicestore.common.config.appProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Frontend frontend,
        ResetToken resetToken,
        Paystack paystack
) {
    public record Frontend(
            String url,
            String resetPasswordPath
    ) {
        public String buildResetUrl(String rawToken) {
            return url + resetPasswordPath + "?token=" + rawToken;
        }
    }

    public record ResetToken(
            long ttlMinutes,
            String emailTemplate
    ) {}


    public record Paystack(
            String secretKey,
            String initializeUrl,
            String verifyUrl,
            String webhookSecret,
            String orderConfirmationEmailTemplate
    ) {}
}
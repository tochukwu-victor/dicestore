package com.victoruk.dicestore.passwordreset.dto;

public record ResetPasswordRequestDto(
        String token,
        String newPassword
) {}
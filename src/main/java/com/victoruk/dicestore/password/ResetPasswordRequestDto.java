package com.victoruk.dicestore.password;

public record ResetPasswordRequestDto(
        String token,
        String newPassword
) {}
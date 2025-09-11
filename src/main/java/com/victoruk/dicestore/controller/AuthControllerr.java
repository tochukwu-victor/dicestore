package com.victoruk.dicestore.controller;


import com.victoruk.dicestore.dto.LoginRequestDto;
import com.victoruk.dicestore.dto.LoginResponseDto;
import com.victoruk.dicestore.dto.RegisterRequestDto;
import com.victoruk.dicestore.dto.RegisterResponseDto;
import com.victoruk.dicestore.password.*;
import com.victoruk.dicestore.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerr {

    private final IAuthService authService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> apiLogin(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        RegisterResponseDto registerResponseDto = authService.register(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponseDto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword(
            @RequestBody ForgotPasswordRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.forgotPassword(requestDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(
            @RequestBody ResetPasswordRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.resetPassword(requestDto));
    }

}

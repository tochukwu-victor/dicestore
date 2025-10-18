package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.*;
import com.victoruk.dicestore.password.*;
import com.victoruk.dicestore.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;   // ✅ add this
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j   // ✅ Lombok generates a logger for you
public class AuthControllerr {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> apiLogin(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login attempt for email: {}", loginRequestDto.email());
        LoginResponseDto response = authService.login(loginRequestDto);
        log.info("Login successful for email: {}", loginRequestDto.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.info("Registration attempt at {} for email: {}", LocalDateTime.now(), registerRequestDto.getEmail());
        RegisterResponseDto registerResponseDto = authService.register(registerRequestDto);
        log.info("User registered successfully with email: {}", registerRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponseDto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword(@RequestBody ForgotPasswordRequestDto requestDto) {
        log.info("Forgot password requested for email: {}", requestDto.email());
        return ResponseEntity.ok(authService.forgotPassword(requestDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@RequestBody ResetPasswordRequestDto requestDto) {
        log.info("Password reset attempt with token: {}", requestDto.token());
        return ResponseEntity.ok(authService.resetPassword(requestDto));
    }
}

package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.LoginRequestDto;
import com.victoruk.dicestore.dto.LoginResponseDto;
import com.victoruk.dicestore.dto.RegisterRequestDto;
import com.victoruk.dicestore.dto.RegisterResponseDto;
import com.victoruk.dicestore.password.ForgotPasswordRequestDto;
import com.victoruk.dicestore.password.ForgotPasswordResponseDto;
import com.victoruk.dicestore.password.ResetPasswordRequestDto;
import com.victoruk.dicestore.password.ResetPasswordResponseDto;
import org.apache.http.auth.InvalidCredentialsException;

public interface IAuthService {
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    RegisterResponseDto register(RegisterRequestDto registerRequestDto);


    ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto);
    ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto);
}

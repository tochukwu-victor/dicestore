package com.victoruk.dicestore.auth.service;

import com.victoruk.dicestore.auth.dto.LoginRequestDto;
import com.victoruk.dicestore.auth.dto.LoginResponseDto;
import com.victoruk.dicestore.auth.dto.RegisterRequestDto;
import com.victoruk.dicestore.auth.dto.RegisterResponseDto;

public interface IAuthService {
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    RegisterResponseDto register(RegisterRequestDto registerRequestDto);

//
//    ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto);
//    ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto);

}

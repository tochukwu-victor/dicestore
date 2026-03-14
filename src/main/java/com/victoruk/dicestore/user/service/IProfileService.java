package com.victoruk.dicestore.user.service;

import com.victoruk.dicestore.user.dto.ProfileRequestDto;
import com.victoruk.dicestore.user.dto.ProfileResponseDto;

public interface IProfileService {

    ProfileResponseDto getProfile();
    ProfileResponseDto updateProfile(ProfileRequestDto profileRequestDto);
}

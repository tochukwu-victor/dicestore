package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.ProfileRequestDto;
import com.victoruk.dicestore.dto.ProfileResponseDto;

public interface IProfileService {

    ProfileResponseDto getProfile();
    ProfileResponseDto updateProfile(ProfileRequestDto profileRequestDto);
}

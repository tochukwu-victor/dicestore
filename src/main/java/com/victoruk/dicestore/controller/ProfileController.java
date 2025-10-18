package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.ProfileRequestDto;
import com.victoruk.dicestore.dto.ProfileResponseDto;
import com.victoruk.dicestore.service.IProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile() {
        log.info("👤 Fetching user profile...");
        ProfileResponseDto responseDto = profileService.getProfile();
        log.info("✅ Profile fetched successfully for user: {}", responseDto.getEmail());
        return ResponseEntity.ok(responseDto);
    }

    // @SecurityRequirement(name = "BearerAuth") // 👈 Enable JWT for Swagger if needed
    @PutMapping
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @Validated @RequestBody ProfileRequestDto profileRequestDto) {
        log.info("✏️ Updating profile for user with email: {}", profileRequestDto.getEmail());
        ProfileResponseDto responseDto = profileService.updateProfile(profileRequestDto);
        log.info("✅ Profile updated successfully for user: {}", responseDto.getEmail());
        return ResponseEntity.ok(responseDto);
    }
}

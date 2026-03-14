package com.victoruk.dicestore.user.controller;

import com.victoruk.dicestore.user.dto.ProfileRequestDto;
import com.victoruk.dicestore.user.dto.ProfileResponseDto;
import com.victoruk.dicestore.user.service.IProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Manage authenticated user profile and address")
public class ProfileController {

    private final IProfileService profileService;

    @GetMapping
    @Operation(summary = "Get current user profile",
            description = "Returns profile and address of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ProfileResponseDto> getProfile() {
        ProfileResponseDto response = profileService.getProfile();
        log.info("Profile fetched for user [{}]", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update current user profile",
            description = "Updates name, mobile number and address. Email cannot be changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed on request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @Validated @RequestBody ProfileRequestDto request) {
        ProfileResponseDto response = profileService.updateProfile(request);
        log.info("Profile updated for user [{}]", response.getEmail());
        return ResponseEntity.ok(response);
    }
}
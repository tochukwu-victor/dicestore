package com.victoruk.dicestore.controller;




import com.victoruk.dicestore.dto.ProfileRequestDto;
import com.victoruk.dicestore.dto.ProfileResponseDto;
import com.victoruk.dicestore.service.IProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor

public class ProfileController {


        private final IProfileService iProfileService;

        @GetMapping
        public ResponseEntity<ProfileResponseDto> getProfile() {
            ProfileResponseDto responseDto = iProfileService.getProfile();
            return ResponseEntity.ok(responseDto);
        }

//        @SecurityRequirement(name = "BearerAuth") // 👈 Enable JWT for all methods
         @PutMapping
        public ResponseEntity<ProfileResponseDto> updateProfile(
                @Validated @RequestBody ProfileRequestDto profileRequestDto) {
            ProfileResponseDto responseDto = iProfileService.updateProfile(profileRequestDto);
            return ResponseEntity.ok(responseDto);
        }

}

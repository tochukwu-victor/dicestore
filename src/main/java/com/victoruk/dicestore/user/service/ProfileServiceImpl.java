package com.victoruk.dicestore.user.service;

import com.victoruk.dicestore.common.security.AuthenticatedUserResolver;
import com.victoruk.dicestore.user.dto.AddressDto;
import com.victoruk.dicestore.user.dto.ProfileRequestDto;
import com.victoruk.dicestore.user.dto.ProfileResponseDto;
import com.victoruk.dicestore.user.entity.Address;
import com.victoruk.dicestore.user.entity.User;
import com.victoruk.dicestore.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;
    private final AuthenticatedUserResolver userResolver;

    @Override
    @Transactional
    public ProfileResponseDto getProfile() {
        User user = userResolver.getAuthenticatedUser();
        log.info("Fetched profile for user [{}]", user.getEmail());
        return toResponseDto(user);
    }

    @Override
    @Transactional
    public ProfileResponseDto updateProfile(ProfileRequestDto request) {
        User user = userResolver.getAuthenticatedUser();
        log.info("Updating profile for user [{}]", user.getEmail());

        // email is intentionally not updated — it is an identity field used for login and JWT
        user.setName(request.getName().trim());
        user.setMobileNumber(request.getMobileNumber().trim());

        Address address = user.getAddress();
        if (address == null) {
            log.debug("No address found for user [{}], creating new one", user.getEmail());
            address = new Address();
            address.setUser(user);
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        user.setAddress(address);

        // CascadeType.ALL on User.address means address saves automatically with user
        user = userRepository.save(user);
        log.info("Profile updated successfully for user [{}]", user.getEmail());

        return toResponseDto(user);
    }

    private ProfileResponseDto toResponseDto(User user) {
        ProfileResponseDto response = new ProfileResponseDto();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());

        if (user.getAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setStreet(user.getAddress().getStreet());
            addressDto.setCity(user.getAddress().getCity());
            addressDto.setState(user.getAddress().getState());
            addressDto.setPostalCode(user.getAddress().getPostalCode());
            addressDto.setCountry(user.getAddress().getCountry());
            response.setAddress(addressDto);
        }

        return response;
    }
}
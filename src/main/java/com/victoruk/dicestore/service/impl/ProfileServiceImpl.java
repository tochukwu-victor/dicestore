

package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.dto.AddressDto;
import com.victoruk.dicestore.dto.ProfileRequestDto;
import com.victoruk.dicestore.dto.ProfileResponseDto;
import com.victoruk.dicestore.entity.Address;
import com.victoruk.dicestore.entity.Customer;
import com.victoruk.dicestore.repository.AddressRepository;
import com.victoruk.dicestore.repository.CustomerRepository;
import com.victoruk.dicestore.service.IProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    @Override
    public ProfileResponseDto getProfile() {
        Customer customer = getAuthenticatedCustomer();
        log.info("Fetched profile for customer [{}]", customer.getEmail());
        return mapCustomerToProfileResponseDto(customer);
    }

    @Override
    public ProfileResponseDto updateProfile(ProfileRequestDto profileRequestDto) {
        Customer customer = getAuthenticatedCustomer();
        log.info("Updating profile for customer [{}]", customer.getEmail());

        boolean isEmailUpdated = !customer.getEmail().equals(profileRequestDto.getEmail().trim());
        BeanUtils.copyProperties(profileRequestDto, customer);

        Address address = customer.getAddress();
        if (address == null) {
            log.debug("No address found for customer [{}], creating a new one", customer.getEmail());
            address = new Address();
            address.setCustomer(customer);
        }

        address.setStreet(profileRequestDto.getStreet());
        address.setCity(profileRequestDto.getCity());
        address.setState(profileRequestDto.getState());
        address.setPostalCode(profileRequestDto.getPostalCode());
        address.setCountry(profileRequestDto.getCountry());
        customer.setAddress(address);

        // Persist the address
        addressRepository.save(address);
        log.debug("Saved/updated address for customer [{}]: {}, {}, {}",
                customer.getEmail(), address.getStreet(), address.getCity(), address.getCountry());

        customer = customerRepository.save(customer);
        log.info("Profile successfully updated for customer [{}]", customer.getEmail());

        ProfileResponseDto profileResponseDto = mapCustomerToProfileResponseDto(customer);
        profileResponseDto.setEmailUpdated(isEmailUpdated);

        if (isEmailUpdated) {
            log.warn("Customer [{}] updated their email. Old: [{}], New: [{}]",
                    customer.getEmail(), customer.getEmail(), profileRequestDto.getEmail());
        }

        return profileResponseDto;
    }

    // Get the authenticated customer
    public Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Fetching authenticated customer with email [{}]", email);

        return customerRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Customer with email [{}] not found in database", email);
            return new UsernameNotFoundException("User not found");
        });
    }

    // Map the customer to the profile response dto
    private ProfileResponseDto mapCustomerToProfileResponseDto(Customer customer) {
        ProfileResponseDto profileResponseDto = new ProfileResponseDto();
        BeanUtils.copyProperties(customer, profileResponseDto);

        if (customer.getAddress() != null) {
            AddressDto addressDto = new AddressDto();
            BeanUtils.copyProperties(customer.getAddress(), addressDto);
            profileResponseDto.setAddress(addressDto);
            log.debug("Mapped address for customer [{}]: {}, {}, {}",
                    customer.getEmail(),
                    customer.getAddress().getStreet(),
                    customer.getAddress().getCity(),
                    customer.getAddress().getCountry());
        } else {
            log.debug("No address found to map for customer [{}]", customer.getEmail());
        }

        return profileResponseDto;
    }
}


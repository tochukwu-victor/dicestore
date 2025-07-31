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
    private final AddressRepository addressRepository; // <-- Add this

    @Override
    public ProfileResponseDto getProfile() {

        Customer customer = getAuthenticatedCustomer();
        return mapCustomerToProfileResponseDto(customer);
    }

    @Override
    public ProfileResponseDto updateProfile(ProfileRequestDto profileRequestDto) {

        Customer customer = getAuthenticatedCustomer();
        boolean isEmailUpdated = !customer.getEmail().equals(profileRequestDto.getEmail().trim());
        BeanUtils.copyProperties(profileRequestDto, customer);
        Address address = customer.getAddress();
        if (address == null) {
            address = new Address();
            address.setCustomer(customer);
        }
        address.setStreet(profileRequestDto.getStreet());
        address.setCity(profileRequestDto.getCity());
        address.setState(profileRequestDto.getState());
        address.setPostalCode(profileRequestDto.getPostalCode());
        address.setCountry(profileRequestDto.getCountry());
        customer.setAddress(address);

        // Persist the address first to ensure it's saved and attached
        addressRepository.save(address); // <-- you need this repository injected
        customer = customerRepository.save(customer);
        ProfileResponseDto profileResponseDto = mapCustomerToProfileResponseDto(customer);
        profileResponseDto.setEmailUpdated(isEmailUpdated);
        return profileResponseDto;
    }


    // Get the authenticated customer
    public Customer getAuthenticatedCustomer(){

       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String email = authentication.getName();
      return customerRepository.findByEmail(email).orElseThrow(() ->
              new UsernameNotFoundException("User not found"));

   }

//   private ProfileResponseDto mapCustomerToProfile(Customer customer){
//
//        ProfileResponseDto profileResponseDto = new ProfileResponseDto();
//
//       BeanUtils.copyProperties(customer,profileResponseDto);
//       return  profileResponseDto;
//   }


    // Map the customer to the profile response dto
    private ProfileResponseDto mapCustomerToProfileResponseDto(Customer customer) {
        ProfileResponseDto profileResponseDto = new ProfileResponseDto();
        BeanUtils.copyProperties(customer, profileResponseDto);
        if (customer.getAddress() != null) {
            AddressDto addressDto = new AddressDto();
            BeanUtils.copyProperties(customer.getAddress(), addressDto);
            profileResponseDto.setAddress(addressDto);
        }
        return profileResponseDto;
    }

}

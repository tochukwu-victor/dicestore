package com.victoruk.dicestore.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProfileResponseDto {
    private Long customerId;
    private String name;
    private String email;
    private String mobileNumber;
    private AddressDto address;
    private boolean emailUpdated;
}


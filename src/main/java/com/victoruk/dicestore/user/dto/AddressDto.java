package com.victoruk.dicestore.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressDto {

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}

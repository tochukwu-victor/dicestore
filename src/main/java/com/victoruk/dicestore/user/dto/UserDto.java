package com.victoruk.dicestore.user.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class UserDto {

    private Long customerId;
    private String name;
    private String email;
    private String mobileNumber;
    private String roles;
    private AddressDto address;


}
package com.victoruk.dicestore.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountResponseDto {
    private Long discountId;
    private BigDecimal percentage;
    private String description;
    private Long productId;
}

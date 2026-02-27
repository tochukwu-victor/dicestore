package com.victoruk.dicestore.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    private Long productId;

    private String name;

    private String description;

    private BigDecimal price;

    private int popularity;

    private BigDecimal discountPercentage;

    private BigDecimal finalPrice;
}

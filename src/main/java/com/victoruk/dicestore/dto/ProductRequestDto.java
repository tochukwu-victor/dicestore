package com.victoruk.dicestore.dto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class ProductRequestDto {

    private Long categoryId;
    private String name;

    private String description;

    private BigDecimal price;

    private int popularity;
}

package com.victoruk.dicestore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiscountRequestDto {
    private BigDecimal percentage;
    private String description;
    private Long productId;

    private LocalDate startDate;
    private LocalDate endDate;

}


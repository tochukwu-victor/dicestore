package com.victoruk.dicestore.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class ProductDto {

    private Long productId;

    private String name;

    private String description;

    private BigDecimal price;

    private int popularity;
}

package com.victoruk.dicestore.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long productId;

    private String name;

    private String description;
    private Integer stock;

    private BigDecimal price;

    private BigDecimal discountPercentage;

    private BigDecimal finalPrice;

    private List<String> imageUrls = new ArrayList<>();


}

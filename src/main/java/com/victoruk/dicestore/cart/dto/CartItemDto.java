package com.victoruk.dicestore.cart.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        BigDecimal discountedPrice,
        Integer quantity,
        BigDecimal lineTotal
) {}
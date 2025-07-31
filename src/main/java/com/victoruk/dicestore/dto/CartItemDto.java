package com.victoruk.dicestore.dto;

import java.math.BigDecimal;

public record CartItemDto(Long productId, String productName,
                          BigDecimal price,
                          Integer quantity) {
}


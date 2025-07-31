
package com.victoruk.dicestore.dto;

import java.math.BigDecimal;

public record OrderItemReponseDto(String productName, Integer quantity,
                                  BigDecimal price) {
}

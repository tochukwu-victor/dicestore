
package com.victoruk.dicestore.order.dto;

import java.math.BigDecimal;

public record OrderItemReponseDto(String productName, Integer quantity,
                                  BigDecimal price) {
}

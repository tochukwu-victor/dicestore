package com.victoruk.dicestore.order.dto;

import com.victoruk.dicestore.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(Long orderId, OrderStatus status,
                               BigDecimal totalPrice, String createdAt,
                               List<OrderItemReponseDto> items) {
}
package com.victoruk.dicestore.common.exception;


import com.victoruk.dicestore.order.entity.OrderStatus;

public class OrderCancellationNotAllowedException extends RuntimeException {
    public OrderCancellationNotAllowedException(Long orderId, OrderStatus currentStatus) {
        super("Order [" + orderId + "] cannot be cancelled. Current status: " + currentStatus);
    }
}

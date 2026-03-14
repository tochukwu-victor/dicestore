package com.victoruk.dicestore.common.exception;

public class OrderAlreadyPaidException extends RuntimeException {
    public OrderAlreadyPaidException(Long orderId) {
        super("Order [" + orderId + "] has already been paid and cannot be re-initialized.");
    }
}
package com.victoruk.dicestore.order.entity;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    FAILED,
    CONFIRMED,
    CANCELLED,
    REFUNDED;

    public boolean isTerminal() {
        return this == PAID || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
}
package com.victoruk.dicestore.order.service;

import com.victoruk.dicestore.order.dto.OrderResponseDto;
import com.victoruk.dicestore.order.entity.OrderStatus;

import java.util.List;

public interface IOrderService {

    OrderResponseDto createOrder();

    List<OrderResponseDto> getCustomerOrders();

    List<OrderResponseDto> getAllPendingOrders();

    void confirmOrder(Long orderId);
    void cancelOrder(Long orderId);        // admin
    void cancelMyOrder(Long orderId);      // customer

}

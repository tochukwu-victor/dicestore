package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.OrderRequestDto;
import com.victoruk.dicestore.dto.OrderResponseDto;

import java.util.List;

public interface IOrderService {

    void  createOrder();

    List<OrderResponseDto> getCustomerOrders();

    List<OrderResponseDto> getAllPendingOrders();

    void updateOrderStatus(Long orderId, String orderStatus);
}

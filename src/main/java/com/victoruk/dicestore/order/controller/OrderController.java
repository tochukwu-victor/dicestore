package com.victoruk.dicestore.order.controller;

import com.victoruk.dicestore.order.dto.OrderResponseDto;
import com.victoruk.dicestore.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder() {
        log.info("🛒 Creating new order...");
        orderService.createOrder();
        log.info("✅ Order created successfully!");
        return ResponseEntity.ok("Order created successfully!");
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> loadCustomerOrders() {
        log.info("📦 Loading customer orders...");
        List<OrderResponseDto> orders = orderService.getCustomerOrders();
        log.info("✅ Loaded {} orders for customer", orders.size());
        return ResponseEntity.ok(orders);
    }
}

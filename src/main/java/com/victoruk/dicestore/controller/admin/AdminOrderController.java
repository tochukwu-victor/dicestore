package com.victoruk.dicestore.controller.admin;

import com.victoruk.dicestore.constant.ApplicationConstants;
import com.victoruk.dicestore.dto.OrderResponseDto;
import com.victoruk.dicestore.dto.ResponseDto;
import com.victoruk.dicestore.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final IOrderService iOrderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllPendingOrders() {
        log.info("Fetching all pending orders");
        return ResponseEntity.ok(iOrderService.getAllPendingOrders());
    }

    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<ResponseDto> confirmOrder(@PathVariable Long orderId) {
        log.info("Confirming order with id: {}", orderId);
        iOrderService.updateOrderStatus(orderId, ApplicationConstants.ORDER_STATUS_CONFIRMED);
        return ResponseEntity.ok(new ResponseDto("200", "Order #" + orderId + " has been approved."));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseDto> cancelOrder(@PathVariable Long orderId) {
        log.warn("Cancelling order with id: {}", orderId);
        iOrderService.updateOrderStatus(orderId, ApplicationConstants.ORDER_STATUS_CANCELLED);
        return ResponseEntity.ok(new ResponseDto("200", "Order #" + orderId + " has been cancelled."));
    }
}
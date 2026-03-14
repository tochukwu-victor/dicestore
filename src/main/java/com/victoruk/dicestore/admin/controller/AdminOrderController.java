package com.victoruk.dicestore.admin.controller;

import com.victoruk.dicestore.common.response.ErrorResponseDto;
import com.victoruk.dicestore.order.dto.OrderResponseDto;
import com.victoruk.dicestore.common.response.ResponseDto;
import com.victoruk.dicestore.order.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Orders", description = "Admin management of customer orders")
public class AdminOrderController {

    private final IOrderService iOrderService;

    @Operation(
            summary = "Get all pending orders",
            description = "Returns all orders currently in PENDING_PAYMENT status awaiting admin review."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending orders fetched successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin only",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllPendingOrders() {
        log.info("Fetching all pending orders");
        return ResponseEntity.ok(iOrderService.getAllPendingOrders());
    }

    @Operation(
            summary = "Confirm an order",
            description = "Moves order status from PAID to CONFIRMED, indicating the order " +
                    "has been reviewed and is being prepared for dispatch. This action " +
                    "is performed by an admin after verifying payment success."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order confirmed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin only",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<ResponseDto> confirmOrder(
            @Parameter(description = "ID of the order to confirm", required = true)
            @PathVariable Long orderId) {
        log.info("Confirming order [{}]", orderId);
        iOrderService.confirmOrder(orderId);
        return ResponseEntity.ok(new ResponseDto("200", "Order #" + orderId + " has been confirmed."));
    }

    @Operation(
            summary = "Cancel an order",
            description = "Moves order status to CANCELLED. This action is irreversible. " +
                    "Should only be performed when payment has failed or the order " +
                    "cannot be fulfilled."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin only",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseDto> cancelOrder(
            @Parameter(description = "ID of the order to cancel", required = true)
            @PathVariable Long orderId) {
        log.warn("Cancelling order [{}]", orderId);
        iOrderService.cancelOrder(orderId);
        return ResponseEntity.ok(new ResponseDto("200", "Order #" + orderId + " has been cancelled."));
    }
}
package com.victoruk.dicestore.order.controller;

import com.victoruk.dicestore.order.dto.OrderResponseDto;
import com.victoruk.dicestore.order.service.IOrderService;
import com.victoruk.dicestore.common.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer order management and retrieval")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Converts the authenticated user's shopping cart into an order. " +
                    "The order is created with PENDING_PAYMENT status and awaits payment processing."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Order created successfully!" }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request — cart may be empty or product out of stock",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — authentication required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart not found for authenticated user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<String> createOrder() {
        log.info("Creating new order for authenticated user");
        orderService.createOrder();
        log.info("Order created successfully");
        return ResponseEntity.status(HttpStatus.OK).body("Order created successfully!");
    }

    @GetMapping
    @Operation(
            summary = "Get current user's orders",
            description = "Retrieves all orders belonging to the authenticated user with their current status, " +
                    "items, total amount, and payment details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — authentication required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No orders found for authenticated user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<List<OrderResponseDto>> loadCustomerOrders() {
        log.info("Loading orders for authenticated user");
        List<OrderResponseDto> orders = orderService.getCustomerOrders();
        log.info("Successfully retrieved {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }
}

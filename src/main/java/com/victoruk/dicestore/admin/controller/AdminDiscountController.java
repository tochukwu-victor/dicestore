package com.victoruk.dicestore.admin.controller;

import com.victoruk.dicestore.discount.dto.DiscountRequestDto;
import com.victoruk.dicestore.discount.dto.DiscountResponseDto;
import com.victoruk.dicestore.discount.service.IDiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/discounts")
@RequiredArgsConstructor
@Tag(name = "Admin - Discounts", description = "Admin management of product discounts")
public class AdminDiscountController {

    private final IDiscountService discountService;

    @PostMapping
    @Operation(summary = "Create a discount",
            description = "Creates a percentage-based discount and links it to a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Discount created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<DiscountResponseDto> createDiscount(@RequestBody DiscountRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountService.createDiscount(dto));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get discounts by product",
            description = "Returns all discounts associated with a specific product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discounts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<DiscountResponseDto>> getDiscountsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(discountService.getDiscountsByProduct(productId));
    }
}
package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.DiscountRequestDto;
import com.victoruk.dicestore.dto.DiscountResponseDto;
import com.victoruk.dicestore.service.IDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final IDiscountService discountService;

    @PostMapping
    public ResponseEntity<DiscountResponseDto> createDiscount(@RequestBody DiscountRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountService.createDiscount(dto));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<DiscountResponseDto>> getDiscountsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(discountService.getDiscountsByProduct(productId));
    }
}

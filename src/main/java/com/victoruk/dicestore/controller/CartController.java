package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.AddToCartRequestDto;
import com.victoruk.dicestore.dto.CartResponseDto;
import com.victoruk.dicestore.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody AddToCartRequestDto requestDto) {
        cartService.addToCart(requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok().build();
    }
}

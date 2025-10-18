package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.AddToCartRequestDto;
import com.victoruk.dicestore.dto.CartResponseDto;
import com.victoruk.dicestore.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final ICartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody AddToCartRequestDto requestDto) {
        log.info("Request to add product {} with quantity {} to cart", requestDto.productId(), requestDto.quantity());
        cartService.addToCart(requestDto);
        log.info("Product {} added successfully to cart", requestDto.productId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart() {
        log.info("Fetching current user's cart");
        CartResponseDto response = cartService.getMyCart();
        log.info("Cart fetched successfully with {} items", response.items().size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        log.info("Request to remove cart item with ID {}", cartItemId);
        cartService.removeItemFromCart(cartItemId);
        log.info("Cart item {} removed successfully", cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        log.info("Request to clear entire cart");
        cartService.clearCart();
        log.info("Cart cleared successfully");
        return ResponseEntity.ok().build();
    }


}

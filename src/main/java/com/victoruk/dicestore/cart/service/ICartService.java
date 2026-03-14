package com.victoruk.dicestore.cart.service;

import com.victoruk.dicestore.cart.dto.AddToCartRequestDto;
import com.victoruk.dicestore.cart.dto.CartResponseDto;

public interface ICartService {
    void addToCart(AddToCartRequestDto requestDto);
    CartResponseDto getMyCart();
    void removeItemFromCart(Long cartItemId);
    void updateQuantity(Long cartItemId, int quantityChange);
    void clearCart();
    int getCartItemCount();
}
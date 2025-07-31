package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.AddToCartRequestDto;
import com.victoruk.dicestore.dto.CartResponseDto;

public interface ICartService {
    void addToCart(AddToCartRequestDto requestDto);
    CartResponseDto getMyCart();
    void removeItemFromCart(Long cartItemId);
    void clearCart();
}

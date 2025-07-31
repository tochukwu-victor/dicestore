package com.victoruk.dicestore.dto;


import java.util.List;

public record CartResponseDto(Long cartId, List<CartItemDto> items) {
}


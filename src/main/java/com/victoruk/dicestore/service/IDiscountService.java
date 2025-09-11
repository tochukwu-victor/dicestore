package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.DiscountRequestDto;
import com.victoruk.dicestore.dto.DiscountResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface IDiscountService {
    DiscountResponseDto createDiscount(DiscountRequestDto dto);
    List<DiscountResponseDto> getDiscountsByProduct(Long productId);
    BigDecimal calculateFinalPrice(Long productId, BigDecimal basePrice);

}

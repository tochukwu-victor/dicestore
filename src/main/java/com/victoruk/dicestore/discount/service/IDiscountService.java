package com.victoruk.dicestore.discount.service;

import com.victoruk.dicestore.discount.dto.DiscountRequestDto;
import com.victoruk.dicestore.discount.dto.DiscountResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface IDiscountService {
    DiscountResponseDto createDiscount(DiscountRequestDto dto);
    List<DiscountResponseDto> getDiscountsByProduct(Long productId);
    BigDecimal calculateFinalPrice(Long productId, BigDecimal basePrice);

}

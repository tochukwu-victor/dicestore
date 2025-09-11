package com.victoruk.dicestore.service.impl;


import com.victoruk.dicestore.dto.DiscountRequestDto;
import com.victoruk.dicestore.dto.DiscountResponseDto;
import com.victoruk.dicestore.entity.Discount;
import com.victoruk.dicestore.entity.Product;
import com.victoruk.dicestore.repository.DiscountRepository;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.IDiscountService;
import com.victoruk.dicestore.util.DiscountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements IDiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Override
    public DiscountResponseDto createDiscount(DiscountRequestDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        discountRepository.findActiveDiscountByProductId(dto.getProductId())
                .ifPresent(existing -> {
                    throw new RuntimeException("An active discount already exists for this product");
                });

        Discount discount = new Discount();
        discount.setPercentage(dto.getPercentage());
        discount.setDescription(dto.getDescription());
        discount.setStartDate(dto.getStartDate());
        discount.setEndDate(dto.getEndDate());
        discount.setProduct(product);

        // ✅ Reuse method
        discount.setActive(DiscountUtils.calculateActiveStatus(discount));

        Discount saved = discountRepository.save(discount);

        DiscountResponseDto response = new DiscountResponseDto();
        response.setDiscountId(saved.getDiscountId());
        response.setPercentage(saved.getPercentage());
        response.setDescription(saved.getDescription());
        response.setProductId(product.getProductId());
        return response;
    }


    @Override
    public List<DiscountResponseDto> getDiscountsByProduct(Long productId) {
        return discountRepository.findAll().stream()
                .filter(d -> d.getProduct().getProductId().equals(productId))
                .map(d -> {
                    DiscountResponseDto dto = new DiscountResponseDto();
                    dto.setDiscountId(d.getDiscountId());
                    dto.setPercentage(d.getPercentage());
                    dto.setDescription(d.getDescription());
                    dto.setProductId(d.getProduct().getProductId());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public BigDecimal calculateFinalPrice(Long productId, BigDecimal basePrice) {
        Discount discount = discountRepository.findActiveDiscountByProductId(productId)
                .orElse(null);
        return DiscountUtils.applyDiscount(basePrice, discount);
    }

}

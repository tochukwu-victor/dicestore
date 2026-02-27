
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements IDiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Override
    public DiscountResponseDto createDiscount(DiscountRequestDto dto) {
        log.info("Creating discount for product [{}] with percentage [{}%]", dto.getProductId(), dto.getPercentage());

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    log.error("Product with ID [{}] not found", dto.getProductId());
                    return new RuntimeException("Product not found");
                });

        discountRepository.findActiveDiscountByProductId(dto.getProductId())
                .ifPresent(existing -> {
                    log.error("Active discount already exists for product [{}]", dto.getProductId());
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
        log.info("Discount [{}] created successfully for product [{}]", saved.getDiscountId(), product.getProductId());

        DiscountResponseDto response = new DiscountResponseDto();
        response.setDiscountId(saved.getDiscountId());
        response.setPercentage(saved.getPercentage());
        response.setDescription(saved.getDescription());
        response.setProductId(product.getProductId());
        return response;
    }

    @Override
    public List<DiscountResponseDto> getDiscountsByProduct(Long productId) {
        log.info("Fetching discounts for product [{}]", productId);

        List<DiscountResponseDto> discounts = discountRepository.findAll().stream()
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

        log.info("Found [{}] discounts for product [{}]", discounts.size(), productId);
        return discounts;
    }

    @Override
    public BigDecimal calculateFinalPrice(Long productId, BigDecimal basePrice) {
        log.debug("Calculating final price for product [{}] with base price [{}]", productId, basePrice);

        Discount discount = discountRepository.findActiveDiscountByProductId(productId).orElse(null);
        BigDecimal finalPrice = DiscountUtils.applyDiscount(basePrice, discount);

        if (discount != null) {
            log.info("Applied discount [{}%] on product [{}]. Final price: [{}]",
                    discount.getPercentage(), productId, finalPrice);
        } else {
            log.info("No active discount for product [{}]. Price remains [{}]", productId, basePrice);
        }

        return finalPrice;
    }
}

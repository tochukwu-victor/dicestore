package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.dto.ProductRequestDto;
import com.victoruk.dicestore.entity.Discount;
import com.victoruk.dicestore.entity.Product;
import com.victoruk.dicestore.dto.ProductDto;
import com.victoruk.dicestore.repository.DiscountRepository;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.IProductService;
import com.victoruk.dicestore.util.DiscountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;


    @Override
    public List<ProductDto> getProducts() {
        log.info("Fetching all products...");

        List<Product> products = productRepository.findAll();

        List<ProductDto> productList = products.stream().map(product -> {
            // ✅ Fetch discount for each product
            Discount discount = discountRepository.findActiveDiscountByProductId(product.getProductId())
                    .orElse(null);

            boolean isActive = DiscountUtils.calculateActiveStatus(discount);

            // ✅ Calculate final price
            BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), discount, isActive);

            // ✅ Build DTO
            ProductDto dto = new ProductDto();
            dto.setProductId(product.getProductId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            dto.setPopularity(product.getPopularity());
            dto.setDiscountPercentage(isActive ? discount.getPercentage() : BigDecimal.ZERO);
            dto.setFinalPrice(finalPrice);

            return dto;
        }).collect(Collectors.toList());

        log.info("Fetched {} products", productList.size());
        return productList;
    }


//    @Override
//    public List<ProductDto> getProducts() {
//        log.info("Fetching all products...");
//        List<ProductDto> productList = productRepository.findAll().stream()
//                .map(this::transformToDto)
//                .collect(Collectors.toList());
//        log.info("Fetched {} products", productList.size());
//        return productList;
//    }

    @Override
    public ProductDto getProductById(Long productId) {
        log.info("Fetching product by ID={}", productId);

        // ✅ Fetch product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID={} not found", productId);
                    return new RuntimeException("Product not found");
                });

        // ✅ Fetch potential discount
        Discount discount = discountRepository.findActiveDiscountByProductId(productId)
                .orElse(null);

        // ✅ Ensure discount active status is recalculated with utility
        boolean isActive = DiscountUtils.calculateActiveStatus(discount);

        // ✅ Calculate final price considering discount validity
        BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), discount, isActive);

        // ✅ Build DTO
        ProductDto productDto = new ProductDto();
        productDto.setProductId(product.getProductId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setDiscountPercentage(isActive ? discount.getPercentage() : BigDecimal.ZERO);
        productDto.setFinalPrice(finalPrice);

        log.info("Fetched product: {}", productDto);
        return productDto;
    }

    private BigDecimal calculateFinalPrice(BigDecimal price, Discount discount, boolean isActive) {
        if (!isActive) {
            return price;
        }
        BigDecimal discountAmount = price.multiply(
                discount.getPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
        return price.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }





    @Override
    public ProductDto createProduct(ProductRequestDto productRequestDto) {
        log.info("Attempting to create product: {}", productRequestDto);

        Product product = toEntity(productRequestDto);

        try {
            Product saved = productRepository.save(product);
            log.info("Product created successfully with ID={}", saved.getProductId());
            return toDto(saved);
        } catch (Exception ex) {
            log.error("Error creating product with name={} and price={}: {}",
                    product.getName(), product.getPrice(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public ProductDto updateProduct(Long productId, ProductRequestDto productRequestDto) {
        log.info("Updating product ID={} with data: {}", productId, productRequestDto);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID={} not found", productId);
                    return new RuntimeException("Product not found");
                });

        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        product.setPopularity(productRequestDto.getPopularity());

        try {
            Product updated = productRepository.save(product);
            log.info("Product updated successfully ID={}", updated.getProductId());
            return toDto(updated);
        } catch (Exception ex) {
            log.error("Error updating product ID={}: {}", productId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        log.info("Deleting product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product with ID={} not found for deletion", productId);
                    return new RuntimeException("Product not found");
                });

        productRepository.delete(product);
        log.info("Deleted product ID={}", productId);
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setPopularity(product.getPopularity());
        return dto;
    }

    private Product toEntity(ProductRequestDto productRequestDto) {
        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        product.setPopularity(productRequestDto.getPopularity());
        return product;
    }

    private ProductDto transformToDto(Product product) {
        ProductDto productDto = new ProductDto();
        BeanUtils.copyProperties(product, productDto);
        return productDto;
    }
}

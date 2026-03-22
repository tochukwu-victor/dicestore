package com.victoruk.dicestore.product.service;

import com.victoruk.dicestore.infrastructure.cloudService.CloudinaryService;
import com.victoruk.dicestore.product.dto.ProductRequestDto;
import com.victoruk.dicestore.product.category.entity.Category;
import com.victoruk.dicestore.discount.entity.Discount;
import com.victoruk.dicestore.product.entity.Product;
import com.victoruk.dicestore.product.dto.ProductDto;
import com.victoruk.dicestore.product.category.repository.CategoryRepository;
import com.victoruk.dicestore.discount.repository.DiscountRepository;
import com.victoruk.dicestore.product.productImage.ProductImage;
import com.victoruk.dicestore.product.repository.ProductRepository;
import com.victoruk.dicestore.discount.service.DiscountUtils;
import com.victoruk.dicestore.common.exception.ProductNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    // ─────────────────────────────────────────────────────────────
    // GET ALL PRODUCTS
    // ─────────────────────────────────────────────────────────────
    @Override
    public List<ProductDto> getProducts() {
        log.info("Fetching all products");

        List<Product> products = productRepository.findAll();

        // One query for all active discounts — fixes N+1
        Map<Long, Discount> discountMap = discountRepository
                .findAllActiveDiscounts()
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getProduct().getProductId(),
                        d -> d
                ));

        List<ProductDto> productList = products.stream().map(product -> {
            Discount discount = discountMap.get(product.getProductId());
            boolean isActive = DiscountUtils.calculateActiveStatus(discount);
            BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), discount, isActive);

            ProductDto dto = new ProductDto();
            dto.setProductId(product.getProductId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            dto.setStock(product.getStock());
            dto.setDiscountPercentage(isActive ? discount.getPercentage() : BigDecimal.ZERO);
            dto.setFinalPrice(finalPrice);
            return dto;
        }).collect(Collectors.toList());

        log.info("Fetched {} products", productList.size());
        return productList;
    }

    // ─────────────────────────────────────────────────────────────
    // GET PRODUCT BY ID
    // ─────────────────────────────────────────────────────────────
    @Override
    public ProductDto getProductById(Long productId) {
        log.info("Fetching product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product ID={} not found", productId);
                    return new ProductNotFoundException("Product Not Found " + productId);

                });

        Discount discount = discountRepository
                .findActiveDiscountByProductId(productId)
                .orElse(null);

        boolean isActive = DiscountUtils.calculateActiveStatus(discount);
        BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), discount, isActive);

        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setDiscountPercentage(isActive ? discount.getPercentage() : BigDecimal.ZERO);
        dto.setFinalPrice(finalPrice);

        log.info("Fetched product ID={}", productId);
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE PRODUCT (with optional images)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ProductDto createProduct(ProductRequestDto dto, List<MultipartFile> files) {
        log.info("Creating product: {}", dto.getName());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found with ID=" + dto.getCategoryId()));
        }

        Product product = toEntity(dto);
        product.setCategory(category);

        Product saved = productRepository.save(product);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                Map<String, Object> result = cloudinaryService.upload(file);
                String imageUrl = (String) result.get("secure_url");
                String publicId = (String) result.get("public_id");
                saved.getImages().add(new ProductImage(imageUrl, publicId, saved));
            }
            productRepository.save(saved);
        }

        log.info("Product created successfully ID={}", saved.getProductId());
        return toDto(saved);
    }


    @Override
    public ProductDto createProduct(ProductRequestDto dto) {
        log.info("Creating product: {}", dto.getName());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found with ID=" + dto.getCategoryId()));
        }

        Product product = toEntity(dto);
        product.setCategory(category);
        Product saved = productRepository.save(product);

        log.info("Product created successfully ID={}", saved.getProductId());
        return toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE PRODUCT
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ProductDto updateProduct(Long productId, ProductRequestDto dto) {
        log.info("Updating product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product ID={} not found for update", productId);
                    return new ProductNotFoundException("Product Not Found " + productId);

                });

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category not found with ID=" + dto.getCategoryId()));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        log.info("Product updated successfully ID={}", updated.getProductId());
        return toDto(updated);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE PRODUCT
    // ─────────────────────────────────────────────────────────────
    @Override
    public void deleteProduct(Long productId) {
        log.info("Deleting product ID={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product ID={} not found for deletion", productId);
                    return new ProductNotFoundException("Product Not Found " + productId);
                });

        productRepository.delete(product);
        log.info("Deleted product ID={}", productId);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────
    private BigDecimal calculateFinalPrice(BigDecimal price,
                                           Discount discount,
                                           boolean isActive) {
        if (!isActive || discount == null) {
            return price;
        }
        BigDecimal discountAmount = price.multiply(
                discount.getPercentage()
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
        return price.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private Product toEntity(ProductRequestDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        return product;
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrls(
                product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .toList()
        );
        return dto;
    }
}

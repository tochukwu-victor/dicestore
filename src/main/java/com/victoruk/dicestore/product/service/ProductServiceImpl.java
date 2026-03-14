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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
            dto.setDiscountPercentage(isActive ? discount.getPercentage() : BigDecimal.ZERO);
            dto.setFinalPrice(finalPrice);

            return dto;
        }).collect(Collectors.toList());

        log.info("Fetched {} products", productList.size());
        return productList;
    }

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

        Category category = null;
        if (productRequestDto.getCategoryId() != null) {
            category = categoryRepository.findById(productRequestDto.getCategoryId())
                    .orElse(null); // optional: just ignore if not found
        }


        Product product = toEntity(productRequestDto);

        product.setCategory(category);

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

    @Transactional
    public ProductDto createProduct(ProductRequestDto dto, List<MultipartFile> files) {
        Product product = toEntity(dto);

        // inline category resolution
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElse(null);
            product.setCategory(category);
        }

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

        List<String> imageUrls = saved.getImages().stream()
                .map(ProductImage::getImageUrl)
                .toList();

        ProductDto productDto = new ProductDto();
        productDto.setProductId(saved.getProductId());
        productDto.setName(saved.getName());
        productDto.setDescription(saved.getDescription());
        productDto.setPrice(saved.getPrice());
        productDto.setImageUrls(imageUrls);
        return productDto;
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
        return dto;
    }

    private ProductDto toDtowithfile(Product product, List<MultipartFile> files) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        return dto;
    }

    private Product toEntity(ProductRequestDto productRequestDto) {
        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        return product;
    }

    private ProductDto transformToDto(Product product) {
        ProductDto productDto = new ProductDto();
        BeanUtils.copyProperties(product, productDto);
        return productDto;
    }
}

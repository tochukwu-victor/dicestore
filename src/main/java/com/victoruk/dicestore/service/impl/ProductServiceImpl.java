package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.dto.ProductRequestDto;
import com.victoruk.dicestore.entity.Product;
import com.victoruk.dicestore.dto.ProductDto;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> getProducts() {

         List<ProductDto> productList = productRepository.findAll().stream().map(
                 this::transformToDto).collect(Collectors.toList());
         return productList;
    }
    @Override
    public ProductDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(product);
    }

    @Override
    public ProductDto createProduct(ProductRequestDto ProductRequestDto) {
        Product product = toEntity(ProductRequestDto);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    @Override
    public ProductDto updateProduct(Long productId, ProductRequestDto productRequestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        product.setPopularity(productRequestDto.getPopularity());
        return toDto(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
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

    private Product toEntity(ProductRequestDto ProductRequestDto) {
        Product product = new Product();
        product.setName(ProductRequestDto.getName());
        product.setDescription(ProductRequestDto.getDescription());
        product.setPrice(ProductRequestDto.getPrice());
        product.setPopularity(ProductRequestDto.getPopularity());
        return product;
    }

    private ProductDto transformToDto(Product product){
        ProductDto productDto = new ProductDto();
        BeanUtils.copyProperties(product,productDto);

        return productDto;
    }
}

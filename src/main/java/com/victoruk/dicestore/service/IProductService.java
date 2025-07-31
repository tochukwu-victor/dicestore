package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.ProductDto;
import com.victoruk.dicestore.dto.ProductRequestDto;

import java.util.List;

public interface IProductService {

    List<ProductDto> getProducts();
    ProductDto getProductById(Long productId);
    ProductDto createProduct(ProductRequestDto dto);
    ProductDto updateProduct(Long productId, ProductRequestDto dto);
    void deleteProduct(Long productId);
}

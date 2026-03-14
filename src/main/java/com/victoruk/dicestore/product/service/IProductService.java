package com.victoruk.dicestore.product.service;

import com.victoruk.dicestore.product.dto.ProductDto;
import com.victoruk.dicestore.product.productImage.ProductImageResponse;
import com.victoruk.dicestore.product.dto.ProductRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductService {

    List<ProductDto> getProducts();
    ProductDto getProductById(Long productId);
    ProductDto createProduct(ProductRequestDto dto);
    ProductDto updateProduct(Long productId, ProductRequestDto dto);
    void deleteProduct(Long productId);

    ProductDto createProduct(ProductRequestDto dto, List<MultipartFile> files);

}

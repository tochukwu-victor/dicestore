package com.victoruk.dicestore.product.controller;

import com.victoruk.dicestore.product.dto.ProductDto;
import com.victoruk.dicestore.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts() throws InterruptedException {
        log.info("📦 Fetching all products...");
        List<ProductDto> productList = productService.getProducts();
        log.info("✅ Found {} products", productList.size());
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        log.info("🔍 Fetching product with ID: {}", id);
        ProductDto product = productService.getProductById(id);
        log.info("✅ Product retrieved: {}", product.getName());
        return ResponseEntity.ok(product);
    }

}

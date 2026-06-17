package com.victoruk.dicestore.product.controller;

import com.victoruk.dicestore.product.dto.ProductDto;
import com.victoruk.dicestore.product.entity.Product;
import com.victoruk.dicestore.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


    @Operation(
            summary = "Fetch all products",
            description = "Retrieve a list of all products in the catalog. Useful for product listings or admin views."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of products"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts() throws InterruptedException {
        log.info("📦 Fetching all products...");
        List<ProductDto> productList = productService.getProducts();
        log.info("✅ Found {} products", productList.size());
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @Operation(
            summary = "Fetch product by ID",
            description = "Retrieve detailed information about a specific product using its unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the product"),
            @ApiResponse(responseCode = "404", description = "Product with given ID not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable Long id) {
        log.info("🔍 Fetching product with ID: {}", id);
        ProductDto product = productService.getProductById(id);
        log.info("✅ Product retrieved: {}", product.getName());
        return ResponseEntity.ok(product);
    }


    @Operation(
            summary = "Search products by keyword",
            description = "Search for products whose name contains the given keyword. Returns a list of matching products."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching products"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameter")
    })
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(
            @Parameter(description = "Keyword to search in product names", required = true)
            @RequestParam String keyword) {

        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

}

package com.victoruk.dicestore.admin.controller;

import com.victoruk.dicestore.product.dto.ProductDto;
import com.victoruk.dicestore.product.dto.ProductRequestDto;
import com.victoruk.dicestore.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Products", description = "Admin management of store products")
public class AdminProductController {

    private final IProductService iProductService;

    @PostMapping
    @Operation(summary = "Create a product",
            description = "Creates a product without images. Use the multipart endpoint to include images on creation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductRequestDto dto) {
        log.info("Request to create product: {}", dto.getName());
        ProductDto created = iProductService.createProduct(dto);
        log.info("Product created with id: {}", created.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a product with images",
            description = "Creates a product and uploads images to Cloudinary in one request. Images are optional.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created with images"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ProductDto> createProductWithImages(
            @RequestPart("data") ProductRequestDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        log.info("Request to create product with images: {}", dto.getName());
        ProductDto created = iProductService.createProduct(dto, files);
        log.info("Product created with id: {} and {} images", created.getProductId(),
                files != null ? files.size() : 0);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product",
            description = "Updates product name, description and price. Does not affect images.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id,
                                                    @RequestBody ProductRequestDto dto) {
        log.info("Request to update product with id: {}", id);
        ProductDto updated = iProductService.updateProduct(id, dto);
        log.info("Product updated successfully with id: {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product",
            description = "Deletes a product and all its associated images from Cloudinary and the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        log.warn("Request to delete product with id: {}", id);
        iProductService.deleteProduct(id);
        log.info("Product deleted successfully with id: {}", id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
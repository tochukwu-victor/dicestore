package com.victoruk.dicestore.admin.controller;

import com.victoruk.dicestore.product.productImage.IproductImageService;
import com.victoruk.dicestore.product.productImage.ProductImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Product Images", description = "Admin management of product images via Cloudinary")
public class AdminProductImageController {

    private final IproductImageService iproductImageService;

    @PostMapping
    @Operation(summary = "Upload a product image",
            description = "Uploads an image to Cloudinary and links it to the specified product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Image uploaded and linked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or missing file parameter"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading image for product id: {}", productId);
        ProductImageResponse response = iproductImageService.uploadProductImage(productId, file);
        log.info("Image uploaded successfully for product id: {}", productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Delete a product image",
            description = "Deletes the image from Cloudinary and removes it from the database using its public ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public ResponseEntity<String> deleteImage(
            @PathVariable Long productId,
            @PathVariable String publicId) {
        log.warn("Deleting image {} for product id: {}", publicId, productId);
        iproductImageService.deleteProductImage(publicId);
        log.info("Image {} deleted successfully", publicId);
        return ResponseEntity.ok("Image deleted successfully");
    }
}
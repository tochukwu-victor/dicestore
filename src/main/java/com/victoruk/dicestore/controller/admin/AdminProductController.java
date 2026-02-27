package com.victoruk.dicestore.controller.admin;


import com.victoruk.dicestore.cloudService.CloudinaryService;
import com.victoruk.dicestore.dto.ProductDto;
import com.victoruk.dicestore.dto.ProductImageResponse;
import com.victoruk.dicestore.dto.ProductRequestDto;
import com.victoruk.dicestore.entity.Product;
import com.victoruk.dicestore.entity.ProductImage;
import com.victoruk.dicestore.repository.ProductImageRepository;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final IProductService iProductService;
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    // ✅ Create product
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductRequestDto dto) {
        long start = System.currentTimeMillis();
        log.info("Request to create product: {}", dto.getName());

        ProductDto created = iProductService.createProduct(dto);

        long duration = System.currentTimeMillis() - start;
        log.info("Product created with id: {} (took {} ms)", created.getProductId(), duration);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ✅ Update product
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDto dto) {
        long start = System.currentTimeMillis();
        log.info("Request to update product with id: {}", id);

        ProductDto updated = iProductService.updateProduct(id, dto);

        long duration = System.currentTimeMillis() - start;
        log.info("Product updated successfully with id: {} (took {} ms)", id, duration);

        return ResponseEntity.ok(updated);
    }


    // ✅ Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        log.warn("Request to delete product with id: {}", id);

        iProductService.deleteProduct(id);

        long duration = System.currentTimeMillis() - start;
        log.info("Product deleted successfully with id: {} (took {} ms)", id, duration);

        return ResponseEntity.ok("Product deleted successfully");
    }


    @Operation(
            summary = "Upload product image",
            description = "Uploads an image to Cloudinary and links it with the given product"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully")
    })
    @PostMapping(
            value = "/{productId}/upload-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @Parameter(
                    description = "Product image file",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        long start = System.currentTimeMillis();
        log.info("Uploading image for product id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", productId);
                    return new RuntimeException("Product not found");
                });

        long cloudStart = System.currentTimeMillis();
        Map<String, Object> uploadResult = cloudinaryService.upload(file);
        long cloudDuration = System.currentTimeMillis() - cloudStart;

        String imageUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        log.info("Image uploaded to Cloudinary. publicId: {}, url: {} (took {} ms)", publicId, imageUrl, cloudDuration);

        ProductImage image = new ProductImage(imageUrl, publicId, product);
        product.getImages().add(image);
        productRepository.save(product);

        long duration = System.currentTimeMillis() - start;
        log.info("Image linked to product id: {} (total {} ms)", productId, duration);

        return ResponseEntity.ok(new ProductImageResponse(imageUrl, publicId));
    }



    // ✅ Delete product image
    @DeleteMapping("/images/{publicId}")
    public ResponseEntity<String> deleteImage(@PathVariable String publicId) throws Exception {
        long start = System.currentTimeMillis();
        log.warn("Request to delete image with publicId: {}", publicId);

        long cloudStart = System.currentTimeMillis();
        cloudinaryService.delete(publicId);
        long cloudDuration = System.currentTimeMillis() - cloudStart;

        imageRepository.deleteByPublicId(publicId);

        long duration = System.currentTimeMillis() - start;
        log.info("Image deleted successfully. publicId: {} (cloud {} ms, total {} ms)", publicId, cloudDuration, duration);

        return ResponseEntity.ok("Image deleted successfully");
    }


}

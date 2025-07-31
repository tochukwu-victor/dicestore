package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.cloudService.CloudinaryService;
import com.victoruk.dicestore.dto.ProductDto;
import com.victoruk.dicestore.dto.ProductImageResponse;
import com.victoruk.dicestore.dto.ProductRequestDto;
import com.victoruk.dicestore.entity.Product;
import com.victoruk.dicestore.entity.ProductImage;
import com.victoruk.dicestore.repository.ProductImageRepository;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;


    private final IProductService iProductService;

    @GetMapping
    public ResponseEntity<List<ProductDto> > getProducts() throws InterruptedException {
        List<ProductDto> productList = iProductService.getProducts();
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductRequestDto ProductRequestDto) {
        ProductDto created = iProductService.createProduct(ProductRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(iProductService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDto productRequestDto) {
        return ResponseEntity.ok(iProductService.updateProduct(id, productRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        iProductService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted successfully");
    }


    //CLOUDINARY SERVICE to upload image
    @PostMapping("/{productId}/upload-image")
    public ResponseEntity<?> uploadProductImage(@PathVariable Long productId,
                                                @RequestParam("file") MultipartFile file) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Map<String, Object> uploadResult = cloudinaryService.upload(file);
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            ProductImage image = new ProductImage();
            image.setUrl(imageUrl);
            image.setPublicId(publicId);
            image.setProduct(product);

            product.getImages().add(image);
            productRepository.save(product); // cascade saves the image

            return ResponseEntity.ok(new ProductImageResponse(imageUrl, publicId));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    //CLOUDINARY SERVICE to delete image
    @DeleteMapping("/images/{publicId}")
    public ResponseEntity<?> deleteImage(@PathVariable String publicId) {
        try {
            cloudinaryService.delete(publicId);
            imageRepository.deleteByPublicId(publicId); // Add this method to repo
            return ResponseEntity.ok("Image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image deletion failed: " + e.getMessage());
        }
    }



}

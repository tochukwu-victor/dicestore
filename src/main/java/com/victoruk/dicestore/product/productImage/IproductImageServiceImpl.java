package com.victoruk.dicestore.product.productImage;

import com.victoruk.dicestore.common.exception.ProductNotFoundException;
import com.victoruk.dicestore.infrastructure.cloudService.CloudinaryService;
import com.victoruk.dicestore.product.entity.Product;
import com.victoruk.dicestore.product.repository.ProductImageRepository;
import com.victoruk.dicestore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;



@Service
@RequiredArgsConstructor
@Slf4j
public class IproductImageServiceImpl implements IproductImageService {



    private final CloudinaryService cloudinaryService;
    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;



    @Override
    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) {
        log.info("Starting image upload for product ID: {}", productId);
        long start = System.currentTimeMillis();

        // 1. Fetch Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // 2. Upload to Cloud (Service handles its own ImageUploadException)
        Map<String, Object> uploadResult = cloudinaryService.upload(file);
        String imageUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        // 3. Link and Save
        ProductImage image = new ProductImage(imageUrl, publicId, product);
        product.getImages().add(image);
        productRepository.save(product);

        log.info("Image linked to product {} in {}ms", productId, System.currentTimeMillis() - start);
        return new ProductImageResponse(imageUrl, publicId);
    }

    @Override
    public void deleteProductImage(String publicId) {
        log.warn("Deleting product image: {}", publicId);
        long start = System.currentTimeMillis();

        // 1. Delete from Cloud
        cloudinaryService.delete(publicId);

        // 2. Delete from DB
        imageRepository.deleteByPublicId(publicId);

        log.info("Image {} deleted successfully in {}ms", publicId, System.currentTimeMillis() - start);
    }

}

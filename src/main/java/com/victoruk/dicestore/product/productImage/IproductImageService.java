package com.victoruk.dicestore.product.productImage;

import org.springframework.web.multipart.MultipartFile;

public interface IproductImageService {

    ProductImageResponse uploadProductImage(Long productId, MultipartFile file);
    void deleteProductImage(String publicId);
}

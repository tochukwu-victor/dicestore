package com.victoruk.dicestore.product.repository;

import com.victoruk.dicestore.product.productImage.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    void deleteByPublicId(String publicId);

}

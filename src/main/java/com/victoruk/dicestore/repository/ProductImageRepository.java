package com.victoruk.dicestore.repository;

import com.victoruk.dicestore.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    void deleteByPublicId(String publicId);

}

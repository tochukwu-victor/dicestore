package com.victoruk.dicestore.product.repository;

import com.victoruk.dicestore.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findById(Long productId);

    List<Product> findByNameContainingIgnoreCase(String name);

    // Fetch products with category and images eagerly to avoid LazyInitializationException
    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.images")
    List<Product> findAllWithCategoryAndImages();


}
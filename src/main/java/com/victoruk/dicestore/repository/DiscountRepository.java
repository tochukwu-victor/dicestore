package com.victoruk.dicestore.repository;

import com.victoruk.dicestore.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    @Query("SELECT d FROM Discount d WHERE d.product.productId = :productId AND d.active = true")
    Optional<Discount> findActiveDiscountByProductId(@Param("productId") Long productId);
}

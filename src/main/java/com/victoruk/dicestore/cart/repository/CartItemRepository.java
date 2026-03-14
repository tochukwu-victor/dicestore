package com.victoruk.dicestore.cart.repository;

import com.victoruk.dicestore.cart.entity.CartItem;
import com.victoruk.dicestore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user = :user")
    List<CartItem> findByCartUser(@Param("user") User user);

    @Query("DELETE FROM CartItem ci WHERE ci.cart.user = :user")
    @org.springframework.data.jpa.repository.Modifying
    void deleteByCartUser(@Param("user") User user);
}
package com.victoruk.dicestore.cart.repository;

import com.victoruk.dicestore.cart.entity.Cart;
import com.victoruk.dicestore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

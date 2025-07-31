package com.victoruk.dicestore.repository;

import com.victoruk.dicestore.entity.Cart;
import com.victoruk.dicestore.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
}

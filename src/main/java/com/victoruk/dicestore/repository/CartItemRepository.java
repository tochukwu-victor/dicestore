package com.victoruk.dicestore.repository;

import com.victoruk.dicestore.entity.CartItem;
import com.victoruk.dicestore.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

//    List<CartItem> findByCartCustomer(Customer customer);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer = :customer")
    List<CartItem> findByCartCustomer(@Param("customer") Customer customer);

}

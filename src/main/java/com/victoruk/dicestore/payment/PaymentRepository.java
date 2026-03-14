package com.victoruk.dicestore.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaystackReference(String paystackReference);
    Optional<Payment> findByOrderOrderId(Long orderId);
}
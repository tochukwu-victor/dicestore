package com.victoruk.dicestore.password;

import com.victoruk.dicestore.entity.Customer;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
//    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime expiryDate;
}


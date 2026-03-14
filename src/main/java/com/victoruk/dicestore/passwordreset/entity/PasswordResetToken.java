package com.victoruk.dicestore.passwordreset.entity;

import com.victoruk.dicestore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Stores a SHA-256 hash of the password reset token.
 * The raw token is NEVER persisted — only emailed to the user.
 */
@Getter
@Setter
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hash of the raw token. Raw token lives only in the email.
     */
    @Column(nullable = false, unique = true)
    private String tokenHash;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Use Instant (UTC) — never LocalDateTime, which has no timezone.
     */
    @Column(nullable = false)
    private Instant expiryDate;

}










//package com.victoruk.dicestore.password;
//
//import com.victoruk.dicestore.user.entity.User;
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//@Data
//@Entity
//@Table(name = "password_reset_tokens")
//public class PasswordResetToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String token;
//
//    @OneToOne
////    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    private LocalDateTime expiryDate;
//}
//

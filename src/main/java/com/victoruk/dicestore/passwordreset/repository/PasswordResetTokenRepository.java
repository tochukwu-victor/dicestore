
package com.victoruk.dicestore.passwordreset.repository;

import com.victoruk.dicestore.passwordreset.entity.PasswordResetToken;
import com.victoruk.dicestore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a token only if it exists AND has not yet expired.
     * Combines both checks into one DB query — eliminates the TOCTOU race condition
     * that exists when you do findByToken() then check expiry separately.
     */
    Optional<PasswordResetToken> findByTokenHashAndExpiryDateAfter(String tokenHash, Instant now);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user);
}





//package com.victoruk.dicestore.password;
//
//import com.victoruk.dicestore.user.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
//    Optional<PasswordResetToken> findByToken(String token);
//    Optional<PasswordResetToken> findByUser(User user);
//}

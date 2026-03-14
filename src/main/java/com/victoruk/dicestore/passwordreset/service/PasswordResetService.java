package com.victoruk.dicestore.passwordreset.service;

import com.victoruk.dicestore.common.config.appProperties.AppProperties;
import com.victoruk.dicestore.common.utility.TokenHashUtil;
import com.victoruk.dicestore.infrastructure.email.EmailService;
import com.victoruk.dicestore.passwordreset.dto.ForgotPasswordRequestDto;
import com.victoruk.dicestore.passwordreset.dto.ForgotPasswordResponseDto;
import com.victoruk.dicestore.passwordreset.dto.ResetPasswordRequestDto;
import com.victoruk.dicestore.passwordreset.dto.ResetPasswordResponseDto;
import com.victoruk.dicestore.passwordreset.entity.PasswordResetToken;
import com.victoruk.dicestore.passwordreset.repository.PasswordResetTokenRepository;
import com.victoruk.dicestore.user.entity.User;
import com.victoruk.dicestore.common.exception.WeakPasswordException;
import com.victoruk.dicestore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles the full password reset lifecycle.
 *
 * Extracted from AuthServiceImpl (SRP fix — auth service was doing too much).
 *
 * Security fixes applied:
 *  1. Token stored as SHA-256 hash — raw token only lives in the email.
 *  2. User enumeration prevented — always returns the same response, even if email not found.
 *  3. Timing attack mitigation — "not found" and "expired" produce the same generic message.
 *  4. TOCTOU race condition fixed — findByTokenHashAndExpiryDateAfter() is a single atomic DB query.
 *  5. Compromised password check applied on reset, not just on registration.
 *  6. Reset URL correctly formed: baseUrl + path + ?token=<rawToken>.
 *  7. Instant used throughout — no LocalDateTime / timezone ambiguity.
 *  8. Both forgotPassword and resetPassword are @Transactional.
 *
 * Rate limiting is handled at the controller/filter layer (see AuthController + RateLimitFilter).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final AppProperties appProperties;

    /**
     * Issues a password reset token and sends the reset email.
     *
     * Security: always returns the same response regardless of whether the email exists.
     * This prevents user enumeration — an attacker cannot distinguish registered
     * from unregistered emails by observing the response.
     */
    @Transactional
    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto) {
        log.info("Processing forgot password request for email: {}", requestDto.email());

        // Always the same response — do NOT leak whether the email is registered
        final ForgotPasswordResponseDto safeResponse = new ForgotPasswordResponseDto(
                "If that email is registered, a reset link has been sent.",
                "OK"
        );

        Optional<User> userOpt = userRepository.findByEmail(requestDto.email());
        if (userOpt.isEmpty()) {
            // Log internally for ops visibility, but return the same response
            log.info("Forgot password request for unregistered email: {} (returning safe response)", requestDto.email());
            return safeResponse;
        }

        User user = userOpt.get();

        // Generate raw token — this is what goes in the email
        String rawToken = UUID.randomUUID().toString();

        // Store only the hash — raw token NEVER touches the DB
        String tokenHash = TokenHashUtil.hash(rawToken);
        long ttlMinutes = appProperties.resetToken().ttlMinutes();

        PasswordResetToken resetToken = tokenRepository.findByUser(user)
                .orElse(new PasswordResetToken());

        resetToken.setTokenHash(tokenHash);
        resetToken.setUser(user);
        resetToken.setExpiryDate(Instant.now().plusSeconds(ttlMinutes * 60));
        tokenRepository.save(resetToken);

        // Build correctly-formed URL: e.g. http://localhost:3000/reset-password?token=<rawToken>
        String resetUrl = appProperties.frontend().buildResetUrl(rawToken);

        // Fire-and-forget (async) — does not block the HTTP thread
        emailService.sendResetPasswordEmail(user.getEmail(), user.getName(), resetUrl);

        log.info("Password reset token issued and email dispatched for user id: {}", user.getUserId());
        return safeResponse;
    }

    /**
     * Validates the reset token and updates the password.
     *
     * Security:
     *  - Hashes the incoming raw token before DB lookup (never store/compare raw tokens).
     *  - Single atomic DB query checks both existence AND expiry (eliminates TOCTOU race).
     *  - "Invalid" and "expired" produce the same generic error message (timing attack mitigation).
     *  - New password is checked against HaveIBeenPwned (compromised password check).
     *  - Token is deleted after use (one-time-use enforced).
     */
    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
        log.info("Password reset attempt received (token hash will be checked)");

        // Hash the incoming raw token before DB lookup
        String tokenHash = TokenHashUtil.hash(requestDto.token());

        // Single atomic query: find token AND check it hasn't expired
        // This eliminates the TOCTOU window that exists when you findByToken then check expiry separately
        PasswordResetToken resetToken = tokenRepository
                .findByTokenHashAndExpiryDateAfter(tokenHash, Instant.now())
                .orElseThrow(() -> {
                    // Same message for "not found" and "expired" — prevents token probing
                    log.warn("Password reset failed: token not found or expired");
                    return new IllegalArgumentException("Invalid or expired reset link");
                });

        // Validate new password strength before saving
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(requestDto.newPassword());
        if (decision.isCompromised()) {
            log.warn("Password reset blocked: new password is compromised for user id: {}", resetToken.getUser().getUserId());
            throw new WeakPasswordException("Choose a stronger password");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(requestDto.newPassword()));
        userRepository.save(user);

        // Delete token — one-time use enforced, also saves inside the same transaction
        tokenRepository.delete(resetToken);

        log.info("Password reset successful for user id: {}", user.getUserId());
        return new ResetPasswordResponseDto("Password reset successful", "OK");
    }
}
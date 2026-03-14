package com.victoruk.dicestore.auth.service;

import com.victoruk.dicestore.auth.dto.LoginRequestDto;
import com.victoruk.dicestore.auth.dto.LoginResponseDto;
import com.victoruk.dicestore.auth.dto.RegisterRequestDto;
import com.victoruk.dicestore.auth.dto.RegisterResponseDto;
import com.victoruk.dicestore.auth.mapper.UserMapper;
import com.victoruk.dicestore.user.entity.Role;
import com.victoruk.dicestore.user.entity.User;
import com.victoruk.dicestore.common.exception.CustomerAlreadyExistsException;
import com.victoruk.dicestore.common.exception.WeakPasswordException;
import com.victoruk.dicestore.infrastructure.jwt.JwtUtil;
import com.victoruk.dicestore.user.repository.UserRepository;
import com.victoruk.dicestore.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Handles user authentication (login) and registration only.
 *
 * Password reset flow has been extracted to PasswordResetService (SRP fix).
 * BeanUtils.copyProperties replaced with UserMapper (safe explicit mapping).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final RoleRepository roleRepository;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Login attempt for email: {}", loginRequestDto.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );

        String jwtToken = jwtUtil.generateJwtToken(authentication);
        log.info("JWT issued for email: {}", authentication.getName());

        // Return a typed DTO rather than a raw String — allows adding expiresAt,
        // tokenType etc. later without a breaking change to the controller contract.
        return new LoginResponseDto(jwtToken);
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        log.info("Registration attempt for email: {}", registerRequestDto.getEmail());

        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(registerRequestDto.getPassword());
        if (decision.isCompromised()) {
            log.warn("Registration failed: compromised password for email: {}", registerRequestDto.getEmail());
            throw new WeakPasswordException("Choose a stronger password");
        }

        Optional<User> existingCustomer = userRepository.findByEmailOrMobileNumber(
                registerRequestDto.getEmail(),
                registerRequestDto.getMobileNumber()
        );

        if (existingCustomer.isPresent()) {
            log.error("Registration failed: email or mobile already registered for: {}", registerRequestDto.getEmail());
            throw new CustomerAlreadyExistsException("Email or mobile number already registered");
        }

        // Safe explicit mapping — replaces BeanUtils.copyProperties
        // Only maps: name, email, mobileNumber. Password is set explicitly below.
        User user = UserMapper.toUser(registerRequestDto);
        user.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        user.setRoles(Set.of(defaultRole));

        userRepository.save(user);

        log.info("Registration successful for email: {}", registerRequestDto.getEmail());
        return new RegisterResponseDto("Registration successful");
    }
}










//package com.victoruk.dicestore.service.impl;
//
//import com.victoruk.dicestore.dto.*;
//import com.victoruk.dicestore.user.entity.User;
//import com.victoruk.dicestore.user.entity.Role;
//import com.victoruk.dicestore.common.exception.CustomerAlreadyExistsException;
//import com.victoruk.dicestore.common.exception.WeakPasswordException;
//import com.victoruk.dicestore.password.*;
//import com.victoruk.dicestore.user.repository.CustomerRepository;
//import com.victoruk.dicestore.user.repository.RoleRepository;
//import com.victoruk.dicestore.auth.service.IAuthService;
//import com.victoruk.dicestore.infrastructure.jwt.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.authentication.password.CompromisedPasswordChecker;
//import org.springframework.security.authentication.password.CompromisedPasswordDecision;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AuthServiceImpl implements IAuthService {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;
//    private final CustomerRepository customerRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final CompromisedPasswordChecker compromisedPasswordChecker;
//    private final RoleRepository roleRepository;
//    private final EmailService emailService;
//    private final PasswordResetTokenRepository tokenRepository;
//
//
//    @Value("${app.frontend.url}")
//    private String frontendBaseUrl;
//
//
//    @Override
//    public String login(LoginRequestDto loginRequestDto) {
//        log.info("🔐 Attempting login for email: {}", loginRequestDto.email());
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequestDto.email(),
//                        loginRequestDto.password()
//                )
//        );
//
//        var loggedInUser = (User) authentication.getPrincipal();
//
//        String jwtToken = jwtUtil.generateJwtToken(authentication);
//
//        log.info("🎫 JWT issued for email: {}", authentication.getName());
//
//        return jwtToken;
//    }
//    @Override
//    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
//        log.info("📝 Attempting registration for email: {}", registerRequestDto.getEmail());
//
//        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(registerRequestDto.getPassword());
//        if (decision.isCompromised()) {
//            log.warn("⚠️ Registration failed: compromised password for email {}", registerRequestDto.getEmail());
//            throw new WeakPasswordException("Choose a stronger password"); // ← fixed here
//        }
//
//        Optional<User> existingCustomer = customerRepository.findByEmailOrMobileNumber(
//                registerRequestDto.getEmail(),
//                registerRequestDto.getMobileNumber()
//        );
//
//        if (existingCustomer.isPresent()) {
//            log.error("❌ Registration failed: email or mobile already registered for {}", registerRequestDto.getEmail());
//            throw new CustomerAlreadyExistsException("Email or mobile number already registered");
//        }
//
//        User user = new User();
//        BeanUtils.copyProperties(registerRequestDto, user);
//        user.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));
//
//        Role defaultRole = roleRepository.findByName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role newRole = new Role();
//                    newRole.setName("ROLE_USER");
//                    return roleRepository.save(newRole);
//                });
//        user.setRoles(Set.of(defaultRole));
//
//        customerRepository.save(user);
//
//        log.info("✅ Registration successful for email: {}", registerRequestDto.getEmail());
//        return new RegisterResponseDto("Registration successful"); // ← only success returns here
//    }
//
//
//    @Override
//    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto) {
//        log.info("🔑 Processing forgot password request for email: {}", requestDto.email());
//
//        User user = customerRepository.findByEmail(requestDto.email())
//                .orElseThrow(() -> {
//                    log.error("❌ Forgot password failed: user not found for email {}", requestDto.email());
//                    return new UsernameNotFoundException("User not found");
//                });
//
//        String token = UUID.randomUUID().toString();
//        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
//
//        PasswordResetToken resetToken = tokenRepository.findByUser(user)
//                .orElse(new PasswordResetToken());
//
//        resetToken.setToken(token);
//        resetToken.setUser(user);
//        resetToken.setExpiryDate(expiry);
//        tokenRepository.save(resetToken);
//
//        emailService.sendResetPasswordEmail(
//                user.getEmail(),
//                user.getName(),
//                frontendBaseUrl + token
//        );
//
//        log.info("📧 Password reset link sent to {}", user.getEmail());
//        return new ForgotPasswordResponseDto(
//                "Password reset link sent to email " + user.getEmail(),
//                "OK"
//        );
//    }
//
//    @Override
//    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
//        log.info("🔄 Attempting password reset with token: {}", requestDto.token());
//
//        PasswordResetToken resetToken = tokenRepository.findByToken(requestDto.token())
//                .orElseThrow(() -> {
//                    log.error("❌ Invalid or expired token used for password reset");
//                    return new IllegalArgumentException("Invalid or expired token");
//                });
//
//        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
//            log.error("⏰ Password reset failed: token expired for email {}", resetToken.getUser().getEmail());
//            throw new IllegalArgumentException("Token has expired");
//        }
//
//        User user = resetToken.getUser();
//        user.setPasswordHash(passwordEncoder.encode(requestDto.newPassword()));
//        customerRepository.save(user);
//
//        tokenRepository.delete(resetToken);
//        log.info("✅ Password reset successful for email {}", user.getEmail());
//
//        return new ResetPasswordResponseDto("Password reset successful", "OK");
//    }
//
//}

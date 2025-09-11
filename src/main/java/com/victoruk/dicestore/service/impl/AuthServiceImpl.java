package com.victoruk.dicestore.service.impl;


import com.victoruk.dicestore.dto.*;
import com.victoruk.dicestore.entity.Customer;
import com.victoruk.dicestore.entity.Role;
import com.victoruk.dicestore.password.*;
import com.victoruk.dicestore.repository.CustomerRepository;
import com.victoruk.dicestore.repository.RoleRepository;
import com.victoruk.dicestore.service.IAuthService;
import com.victoruk.dicestore.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final RoleRepository roleRepository;
    private final EmailService emailService; // assume you have this
    private final PasswordResetTokenRepository tokenRepository;



    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for username: {}", loginRequestDto.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.username(),
                        loginRequestDto.password()
                )
        );

        var loggedInUser = (Customer) authentication.getPrincipal();

        // Build UserDto
        var userDto = new UserDto();
        BeanUtils.copyProperties(loggedInUser, userDto);
        userDto.setRoles(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));

        if (loggedInUser.getAddress() != null) {
            var addressDto = new AddressDto();
            BeanUtils.copyProperties(loggedInUser.getAddress(), addressDto);
            userDto.setAddress(addressDto);
        }

        String jwtToken = jwtUtil.generateJwtToken(authentication);
        log.info("Authentication successful for user: {}", authentication.getName());

        return new LoginResponseDto("OK", userDto, jwtToken);
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(registerRequestDto.getPassword());
        if (decision.isCompromised()) {
            return new RegisterResponseDto("Choose a stronger password");
        }

        Optional<Customer> existingCustomer = customerRepository.findByEmailOrMobileNumber(
                registerRequestDto.getEmail(),
                registerRequestDto.getMobileNumber()
        );

        if (existingCustomer.isPresent()) {
            throw new IllegalArgumentException("Email or mobile number already registered");
        }

        Customer customer = new Customer();
        BeanUtils.copyProperties(registerRequestDto, customer);
        customer.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found in database"));

        customer.setRoles(Set.of(role));
        customerRepository.save(customer);

        return new RegisterResponseDto("Registration successful");
    }

    @Override
    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto) {
        Customer customer = customerRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        // check if user already has a token
        PasswordResetToken resetToken = tokenRepository.findByCustomer(customer)
                .orElse(new PasswordResetToken());

        resetToken.setToken(token);
        resetToken.setCustomer(customer);
        resetToken.setExpiryDate(expiry);

        tokenRepository.save(resetToken); // will update if exists

//        String resetLink = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(
                customer.getEmail(),
                customer.getName(),
                "http://localhost:8080/api/v1/auth/reset-password?token=" + token
//                "Click the link to reset your password: " + resetLink
        );

        return new ForgotPasswordResponseDto(
                "Password reset link sent to email " + customer.getEmail(),
                "OK"
        );
    }


    @Override
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
        PasswordResetToken resetToken = tokenRepository.findByToken(requestDto.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        Customer customer = resetToken.getCustomer();
        customer.setPasswordHash(passwordEncoder.encode(requestDto.newPassword()));
        customerRepository.save(customer);

        tokenRepository.delete(resetToken); // invalidate token

        return new ResetPasswordResponseDto("Password reset successful", "OK");
    }


}

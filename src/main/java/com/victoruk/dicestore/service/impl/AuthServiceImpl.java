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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;


    @Value("${app.frontend.url}")
    private String frontendBaseUrl;


    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("🔐 Attempting login for email: {}", loginRequestDto.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );

        var loggedInUser = (Customer) authentication.getPrincipal();
        log.debug("✅ Authentication successful for email: {}, roles: {}",
                loggedInUser.getEmail(),
                authentication.getAuthorities());

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
        log.info("🎫 JWT issued for email: {}", authentication.getName());

        return new LoginResponseDto("OK", userDto, jwtToken);
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        log.info("📝 Attempting registration for email: {}", registerRequestDto.getEmail());

        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(registerRequestDto.getPassword());
        if (decision.isCompromised()) {
            log.warn("⚠️ Registration failed: compromised password for email {}", registerRequestDto.getEmail());
            return new RegisterResponseDto("Choose a stronger password");
        }

        Optional<Customer> existingCustomer = customerRepository.findByEmailOrMobileNumber(
                registerRequestDto.getEmail(),
                registerRequestDto.getMobileNumber()
        );

        if (existingCustomer.isPresent()) {
            log.error("❌ Registration failed: email or mobile already registered for {}", registerRequestDto.getEmail());
            throw new IllegalArgumentException("Email or mobile number already registered");
        }

        Customer customer = new Customer();
        BeanUtils.copyProperties(registerRequestDto, customer);
        customer.setPasswordHash(passwordEncoder.encode(registerRequestDto.getPassword()));


        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        customer.setRoles(Set.of(defaultRole));

        customerRepository.save(customer);

        log.info("✅ Registration successful for email: {}", registerRequestDto.getEmail());
        return new RegisterResponseDto("Registration successful");
    }

//    Role defaultRole = roleRepository.findByName("ROLE_USER")
//            .orElseThrow(() -> new RuntimeException("Default role not found"));
//customer.setRoles(Set.of(defaultRole));


    @Override
    public ForgotPasswordResponseDto forgotPassword(ForgotPasswordRequestDto requestDto) {
        log.info("🔑 Processing forgot password request for email: {}", requestDto.email());

        Customer customer = customerRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> {
                    log.error("❌ Forgot password failed: user not found for email {}", requestDto.email());
                    return new UsernameNotFoundException("User not found");
                });

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = tokenRepository.findByCustomer(customer)
                .orElse(new PasswordResetToken());

        resetToken.setToken(token);
        resetToken.setCustomer(customer);
        resetToken.setExpiryDate(expiry);
        tokenRepository.save(resetToken);

        emailService.sendResetPasswordEmail(
                customer.getEmail(),
                customer.getName(),
                frontendBaseUrl + token
        );

        log.info("📧 Password reset link sent to {}", customer.getEmail());
        return new ForgotPasswordResponseDto(
                "Password reset link sent to email " + customer.getEmail(),
                "OK"
        );
    }

    @Override
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
        log.info("🔄 Attempting password reset with token: {}", requestDto.token());

        PasswordResetToken resetToken = tokenRepository.findByToken(requestDto.token())
                .orElseThrow(() -> {
                    log.error("❌ Invalid or expired token used for password reset");
                    return new IllegalArgumentException("Invalid or expired token");
                });

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.error("⏰ Password reset failed: token expired for email {}", resetToken.getCustomer().getEmail());
            throw new IllegalArgumentException("Token has expired");
        }

        Customer customer = resetToken.getCustomer();
        customer.setPasswordHash(passwordEncoder.encode(requestDto.newPassword()));
        customerRepository.save(customer);

        tokenRepository.delete(resetToken);
        log.info("✅ Password reset successful for email {}", customer.getEmail());

        return new ResetPasswordResponseDto("Password reset successful", "OK");
    }

}

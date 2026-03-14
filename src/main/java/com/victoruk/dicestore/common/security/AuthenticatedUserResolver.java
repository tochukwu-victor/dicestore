package com.victoruk.dicestore.common.security;

import com.victoruk.dicestore.user.entity.User;
import com.victoruk.dicestore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Loading user data for authenticated email [{}]", email);

        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Authenticated user [{}] not found in database", email);
            return new UsernameNotFoundException("User not found");
        });
    }
}
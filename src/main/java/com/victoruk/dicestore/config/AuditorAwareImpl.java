package com.victoruk.dicestore.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {


    //AuditorAwareImpl class is used to get the current logged in user email address and set it as an auditor
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(
                        org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                )
                .filter(auth -> auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                .map(org.springframework.security.core.Authentication::getName);
    }
}


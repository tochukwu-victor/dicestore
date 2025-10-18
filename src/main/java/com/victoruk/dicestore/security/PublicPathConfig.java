package com.victoruk.dicestore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class      PublicPathConfig {

    @Bean
    public List<String> publicPaths() {

        return List.of(

                "/",                     // root is public
                "/api/v1/products/**",
                "/api/v1/contacts/**",
                "/api/v1/auth/**",
                "/error",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );

    }
}

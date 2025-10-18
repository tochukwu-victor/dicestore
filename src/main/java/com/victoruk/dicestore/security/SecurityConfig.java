package com.victoruk.dicestore.security;
import com.victoruk.dicestore.config.CorsProperties;
import com.victoruk.dicestore.filter.JWTTokenValidatorFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final List<String> publicPaths;
    private final CorsProperties corsProperties;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http
                                      ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(corsConfig -> corsConfig.configurationSource(configurationSource()))
                .authorizeHttpRequests((requests) -> {
                    publicPaths
                            .forEach(path -> requests.requestMatchers(path).permitAll());

                    requests.requestMatchers("/api/v1/admin/**").hasAnyRole("USER", "ADMIN");
                    requests.requestMatchers("/eazystore/actuator/**").hasRole("DEV");
//                    requests.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
//                            "/v3/api-docs/**").hasAnyRole("DEV", "USER", "ADMIN");
                    requests.anyRequest().hasAnyRole("USER", "ADMIN");
                })
                .addFilterBefore(new JWTTokenValidatorFilter(publicPaths), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
      var providerManager = new ProviderManager(authenticationProvider);
      return providerManager;
    }


    // /password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //compromised password checker
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker(){
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    //cors configuration
    @Bean
    public CorsConfigurationSource configurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}

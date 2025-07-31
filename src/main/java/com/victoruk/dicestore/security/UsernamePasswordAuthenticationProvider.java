package com.victoruk.dicestore.security;

import com.victoruk.dicestore.entity.Customer;
import com.victoruk.dicestore.entity.Role;
import com.victoruk.dicestore.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class UsernamePasswordAuthenticationProvider implements  AuthenticationProvider {

        private final CustomerRepository customerRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = authentication.getName();
            String pwd = authentication.getCredentials().toString();

            Customer customer = customerRepository.findByEmail(username).orElseThrow(
                    () -> new UsernameNotFoundException(
                            "User details not found for the user: " + username)
            );
            Set<Role> roles = customer.getRoles();
            List<SimpleGrantedAuthority> authorities = roles.stream().map(
                    role -> new SimpleGrantedAuthority(role.getName())).toList();
            if(passwordEncoder.matches(pwd, customer.getPasswordHash())) {
                return new UsernamePasswordAuthenticationToken(customer,null,
                        authorities);
            } else {
                throw new BadCredentialsException("Invalid password!");
            }
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
        }

}

package com.victoruk.dicestore.auth.service;

import com.victoruk.dicestore.user.entity.User;
import com.victoruk.dicestore.user.entity.Role;
import com.victoruk.dicestore.user.repository.UserRepository;
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

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = authentication.getName();
            String pwd = authentication.getCredentials().toString();

            User user = userRepository.findByEmailWithRoles(username).orElseThrow(
                    () -> new UsernameNotFoundException(
                            "User details not found for the user: " + username)
            );
            Set<Role> roles = user.getRoles();
            List<SimpleGrantedAuthority> authorities = roles.stream().map(
                    role -> new SimpleGrantedAuthority(role.getName())).toList();
            if(passwordEncoder.matches(pwd, user.getPasswordHash())) {
                return new UsernamePasswordAuthenticationToken(user,null,
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

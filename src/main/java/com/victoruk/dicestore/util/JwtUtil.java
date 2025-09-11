package com.victoruk.dicestore.util;


import com.victoruk.dicestore.constant.ApplicationConstants;
import com.victoruk.dicestore.entity.Customer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final Environment env;

    public  String generateJwtToken(Authentication authentication){

        String jwt =  " ";

        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Customer fetchedUser = (Customer) authentication.getPrincipal();
        jwt = Jwts.builder().issuer("Dice Store").subject("JWT Token")
                .claim("username", fetchedUser.getEmail())
                .claim("email", fetchedUser.getEmail())
                .claim("mobileNumber", fetchedUser.getMobileNumber())
                .claim("roles" , authentication.getAuthorities().stream().
                        map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date((new java.util.Date()).getTime() + 30L * 24 * 60 * 60 * 1000))

                .signWith(secretKey).compact();
        return jwt;

    }

}

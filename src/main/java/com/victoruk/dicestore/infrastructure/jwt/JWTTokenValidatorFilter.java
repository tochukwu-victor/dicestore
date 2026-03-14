package com.victoruk.dicestore.infrastructure.jwt;

import com.victoruk.dicestore.common.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> publicPaths;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(ApplicationConstants.JWT_HEADER);
        String jwt = null;

        // 1️⃣ Original header logic
        if (authHeader != null) {
            authHeader = authHeader.trim();
            if (!authHeader.startsWith("Bearer ")) {
                throw new BadCredentialsException("Missing 'Bearer' prefix");
            }
            String[] parts = authHeader.split("\\s+");
            jwt = parts[parts.length - 1]; // last part
        }

        // 2️⃣ If no header, try HttpOnly cookie
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // 3️⃣ Validate JWT if found
        if (jwt != null) {
            try {
                Environment env = getEnvironment();
                if (env != null) {
                    String secret = env.getProperty(
                            ApplicationConstants.JWT_SECRET_KEY,
                            ApplicationConstants.JWT_SECRET_DEFAULT_VALUE
                    );
                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(jwt)
                            .getPayload();

                    String username = String.valueOf(claims.get("email"));
                    String roles = String.valueOf(claims.get("roles"));
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            AuthorityUtils.commaSeparatedStringToAuthorityList(roles)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token Expired");
                return;
            } catch (Exception e) {
                throw new BadCredentialsException("Invalid Token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
            throws ServletException {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(publicPath ->
                pathMatcher.match(publicPath, path));
    }
}








//package com.victoruk.dicestore.infrastructure.jwt;
//
//import com.victoruk.dicestore.common.constants.ApplicationConstants;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.env.Environment;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.crypto.SecretKey;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@RequiredArgsConstructor
//public class JWTTokenValidatorFilter extends OncePerRequestFilter {
//
//    private final AntPathMatcher pathMatcher = new AntPathMatcher();
//    private final List<String> publicPaths;
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        String authHeader = request.getHeader(ApplicationConstants.JWT_HEADER);
//        if (authHeader != null) {
//            try {
//                // Normalize the header (trim + split)
//                authHeader = authHeader.trim();
//                if (!authHeader.startsWith("Bearer ")) {
//                    throw new BadCredentialsException("Missing 'Bearer' prefix");
//                }
//
//                // Handle duplicate "Bearer" by splitting on whitespace
//                String[] parts = authHeader.split("\\s+");
//                String jwt = parts[parts.length - 1]; // Get last part (token)
//
//                // Validate JWT
//                Environment env = getEnvironment();
//                if (env != null) {
//                    String secret = env.getProperty(
//                            ApplicationConstants.JWT_SECRET_KEY,
//                            ApplicationConstants.JWT_SECRET_DEFAULT_VALUE
//                    );
//                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//                    Claims claims = Jwts.parser()
//                            .verifyWith(secretKey)
//                            .build()
//                            .parseSignedClaims(jwt)
//                            .getPayload();
//
//                    // Set authentication
//                    String username = String.valueOf(claims.get("email"));
//                    String roles = String.valueOf(claims.get("roles"));
//                    Authentication auth = new UsernamePasswordAuthenticationToken(
//                            username,
//                            null,
//                            AuthorityUtils.commaSeparatedStringToAuthorityList(roles)
//                    );
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//                }
//            } catch (ExpiredJwtException e) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Token Expired");
//                return;
//            } catch (Exception e) {
//                throw new BadCredentialsException("Invalid Token: " + e.getMessage());
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request)
//            throws ServletException {
//        String path = request.getRequestURI();
//        return publicPaths.stream().anyMatch(publicPath ->
//                pathMatcher.match(publicPath, path));
//    }
//}
//

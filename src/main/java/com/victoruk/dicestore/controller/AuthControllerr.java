
package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.*;
import com.victoruk.dicestore.password.ForgotPasswordRequestDto;
import com.victoruk.dicestore.password.ForgotPasswordResponseDto;
import com.victoruk.dicestore.password.ResetPasswordRequestDto;
import com.victoruk.dicestore.password.ResetPasswordResponseDto;
import com.victoruk.dicestore.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Authentication endpoints (cookie-based JWT)")
public class AuthControllerr {

    private final IAuthService authService;

    @Operation(
            summary = "Login (sets HttpOnly JWT cookie)",
            description = """
                    Authenticates a user and sets an HttpOnly cookie named `jwt`.
                    
                    - The JWT is NOT returned in the response body.
                    - The browser stores the cookie and automatically sends it on subsequent requests.
                    - Frontend should call `/api/v1/profile` (or your profile endpoint) to get the logged-in user.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful (cookie set)",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "HttpOnly cookie containing JWT (e.g., jwt=...; Path=/; HttpOnly; SameSite=Lax)",
                            schema = @Schema(type = "string")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Login successful" }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Email and password are required" }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Invalid email or password" }
                                    """))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> apiLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDto.class),
                            examples = @ExampleObject(value = """
                                    { "email": "user@example.com", "password": "Password123!" }
                                    """)
                    )
            )
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ) {
        log.info("Login attempt for email: {}", loginRequestDto.email());

        String token = authService.login(loginRequestDto);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // true in production (HTTPS)
                .path("/")
                .maxAge(60 * 60 * 24) // 1 day
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("Login successful for email: {}", loginRequestDto.email());
        return ResponseEntity.ok(new LoginResponseDto("Login successful"));
    }

    @Operation(
            summary = "Logout (clears JWT cookie)",
            description = """
                    Clears the HttpOnly `jwt` cookie by setting Max-Age=0.
                    After logout, protected endpoints will return 401.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful (cookie cleared)",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "Clears jwt cookie (jwt=; Max-Age=0; Path=/; HttpOnly; ...)",
                            schema = @Schema(type = "string")
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // true in production with https
                .path("/")
                .maxAge(0) // delete cookie
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new account. Does not log the user in automatically unless you choose to."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Registration successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Registration successful" }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Choose a stronger password" }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email or mobile already registered",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Email or mobile number already registered" }
                                    """))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Registration data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequestDto.class)
                    )
            )
            @Valid @RequestBody RegisterRequestDto registerRequestDto
    ) {
        log.info("Registration attempt at {} for email: {}", LocalDateTime.now(), registerRequestDto.getEmail());
        RegisterResponseDto registerResponseDto = authService.register(registerRequestDto);
        log.info("User registered successfully with email: {}", registerRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponseDto);
    }

    @Operation(
            summary = "Request password reset",
            description = """
                    Sends a password reset link/token to the user's email if the account exists.
                    For security, you may choose to always return 200 even if user doesn't exist.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reset link sent (or request accepted)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForgotPasswordResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Email for password reset",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ForgotPasswordRequestDto.class),
                            examples = @ExampleObject(value = """
                                    { "email": "user@example.com" }
                                    """)
                    )
            )
            @RequestBody ForgotPasswordRequestDto requestDto
    ) {
        log.info("Forgot password requested for email: {}", requestDto.email());
        return ResponseEntity.ok(authService.forgotPassword(requestDto));
    }

    @Operation(
            summary = "Reset password using token",
            description = "Resets password using the token that was emailed to the user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResetPasswordResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Password reset successful", "status": "OK" }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Invalid or expired token" }
                                    """))
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Reset token and new password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResetPasswordRequestDto.class)
                    )
            )
            @RequestBody ResetPasswordRequestDto requestDto
    ) {
        log.info("Password reset attempt with token: {}", requestDto.token());
        return ResponseEntity.ok(authService.resetPassword(requestDto));
    }
}



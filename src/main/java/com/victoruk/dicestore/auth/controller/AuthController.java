package com.victoruk.dicestore.auth.controller;

import com.victoruk.dicestore.auth.dto.LoginRequestDto;
import com.victoruk.dicestore.auth.dto.LoginResponseDto;
import com.victoruk.dicestore.auth.dto.RegisterRequestDto;
import com.victoruk.dicestore.auth.dto.RegisterResponseDto;
import com.victoruk.dicestore.passwordreset.dto.ForgotPasswordRequestDto;
import com.victoruk.dicestore.passwordreset.dto.ForgotPasswordResponseDto;
import com.victoruk.dicestore.passwordreset.dto.ResetPasswordRequestDto;
import com.victoruk.dicestore.passwordreset.dto.ResetPasswordResponseDto;
import com.victoruk.dicestore.auth.service.IAuthService;
import com.victoruk.dicestore.passwordreset.service.PasswordResetService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Authentication endpoints (cookie-based JWT)")
public class AuthController {

    private final IAuthService authService;
    private final PasswordResetService passwordResetService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Operation(
            summary = "Login (sets HttpOnly JWT cookie)",
            description = """
                    Authenticates a user and sets an HttpOnly cookie named `jwt`.
                    The JWT is NOT returned in the response body.
                    The browser stores the cookie and automatically sends it on subsequent requests.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful (cookie set)",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "HttpOnly Secure cookie containing JWT",
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
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ) {
        log.info("Login attempt for email: {}", loginRequestDto.email());

        LoginResponseDto loginResponse = authService.login(loginRequestDto);

        ResponseCookie cookie = ResponseCookie.from("jwt", loginResponse.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("Login successful for email: {}", loginRequestDto.email());
        return ResponseEntity.ok(new LoginResponseDto("Login successful"));
    }

    @Operation(
            summary = "Logout (clears JWT cookie)",
            description = "Clears the HttpOnly `jwt` cookie by setting Max-Age=0."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successful (cookie cleared)")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new account. Does not log the user in automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Registration successful" }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Validation failed or weak password",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Email or mobile already registered",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(
            @Valid @RequestBody RegisterRequestDto registerRequestDto
    ) {
        log.info("Registration attempt for email: {}", registerRequestDto.getEmail());
        RegisterResponseDto response = authService.register(registerRequestDto);
        log.info("Registration successful for email: {}", registerRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Request password reset",
            description = """
                    Sends a password reset link to the user's email if the account exists.
                    Always returns 200 regardless of whether the email is registered (prevents user enumeration).
                    Rate limited to 5 requests per minute per IP.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request accepted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ForgotPasswordResponseDto.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/forgot-password")
    @RateLimiter(name = "forgotPassword", fallbackMethod = "forgotPasswordRateLimitFallback")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto requestDto
    ) {
        log.info("Forgot password request received");
        return ResponseEntity.ok(passwordResetService.forgotPassword(requestDto));
    }

    public ResponseEntity<ForgotPasswordResponseDto> forgotPasswordRateLimitFallback(
            ForgotPasswordRequestDto requestDto,
            Throwable throwable
    ) {
        log.warn("Rate limit exceeded on /forgot-password");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ForgotPasswordResponseDto("Too many requests. Please try again later.", "RATE_LIMITED"));
    }

    @Operation(
            summary = "Reset password using token",
            description = "Resets password using the one-time token emailed to the user. Token expires in 30 minutes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResetPasswordResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Password reset successful", "status": "OK" }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired reset link",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "New password is too weak",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto requestDto
    ) {
        log.info("Password reset attempt received");
        return ResponseEntity.ok(passwordResetService.resetPassword(requestDto));
    }
}
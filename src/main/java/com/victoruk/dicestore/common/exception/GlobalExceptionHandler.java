package com.victoruk.dicestore.common.exception;

import com.victoruk.dicestore.common.response.ErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderAlreadyPaidException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderAlreadyPaid(
            OrderAlreadyPaidException ex, WebRequest wr) {
        log.warn("Attempted to re-initialize an already paid order: {}", ex.getMessage());
        return buildResponse(wr, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OrderCancellationNotAllowedException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderCancellationNotAllowed(
            OrderCancellationNotAllowedException ex, WebRequest wr) {
        log.warn("Order cancellation not allowed: {}", ex.getMessage());
        return buildResponse(wr, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PaymentInitializationException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentInitialization(PaymentInitializationException ex, WebRequest wr) {
        log.error("Payment initialization failed: {}", ex.getMessage());
        return buildResponse(wr, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // Helper method to keep response structure consistent
    private ResponseEntity<ErrorResponseDto> buildResponse(WebRequest webRequest, HttpStatus status, String message) {
        ErrorResponseDto dto = new ErrorResponseDto(webRequest.getDescription(false), status, message, Instant.now());
        return new ResponseEntity<>(dto, status);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProductNotFound(ProductNotFoundException ex, WebRequest wr) {
        return buildResponse(wr, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, WebRequest wr) {
        log.error("Internal Server Error: ", ex);
        return buildResponse(wr, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex, WebRequest wr) {
        return buildResponse(wr, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest wr) {
        String msg = (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("constraint"))
                ? "A record with that unique value already exists."
                : "Database integrity violation.";
        return buildResponse(wr, HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex, WebRequest wr) {
        return buildResponse(wr, HttpStatus.UNAUTHORIZED, "Authentication failed. Please check your credentials.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex, WebRequest wr) {
        return buildResponse(wr, HttpStatus.FORBIDDEN, "You don’t have permission to perform this action.");
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleWeakPassword(WeakPasswordException ex, WebRequest wr) {
        return buildResponse(wr, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomerExists(CustomerAlreadyExistsException ex, WebRequest wr) {
        return buildResponse(wr, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ErrorResponseDto> handleImageUpload(ImageUploadException ex, WebRequest wr) {
        log.error("Image Upload Error: {} | Cause: {}", ex.getMessage(), ex.getCause());
        return buildResponse(wr, ex.getStatus(), ex.getMessage());
    }

    // Specific handlers for validation that return a map of field errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
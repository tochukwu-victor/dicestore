
package com.victoruk.dicestore.common.exception;


import org.springframework.http.HttpStatus;

public class ImageUploadException extends RuntimeException {
    private final HttpStatus status;

    // Use this for validation errors (e.g., "File too large")
    public ImageUploadException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // Use this for technical failures (e.g., AWS S3 is down)
    public ImageUploadException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
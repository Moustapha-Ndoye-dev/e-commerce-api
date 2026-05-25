package com.ecommerce.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI(), List.of(ex.getMessage()));
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, String path, List<String> details) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details);
        return ResponseEntity.status(status).body(body);
    }
}

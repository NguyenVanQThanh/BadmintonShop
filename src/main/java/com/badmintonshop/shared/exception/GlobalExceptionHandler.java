package com.badmintonshop.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.badmintonshop.admin.payload.response.ErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler - Centralized Error Handling.
 * <p>
 * This class acts as an Aspect-Oriented component that intercepts exceptions
 * thrown from any Controller, Service, or Repository layer.
 * It standardizes all error responses using the {@link ErrorResponse} DTO.
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========================================================================
    // 1. SECURITY & AUTHENTICATION ERRORS (401, 403)
    // ========================================================================

    /**
     * Handles authentication failures, typically due to incorrect email or password.
     * <p>
     * HTTP Status: 401 Unauthorized
     * </p>
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        // Log a warning with the IP address for security auditing
        log.warn("Login failed: Invalid credentials from IP: {}", request.getRemoteAddr());
        
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Email hoặc mật khẩu không chính xác.") // User-facing message (Vietnamese)
                .path(request.getRequestURI())
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles cases where the user account is disabled or locked.
     * <p>
     * HTTP Status: 401 Unauthorized
     * </p>
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex, HttpServletRequest request) {
        log.warn("Login failed: Account is locked. User Principal: {}", request.getUserPrincipal());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Account Locked")
                .message("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles authorization failures (e.g., a STAFF trying to access an ADMIN endpoint).
     * <p>
     * HTTP Status: 403 Forbidden
     * </p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access Denied: User attempted to access protected resource: {}", request.getRequestURI());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Bạn không có quyền thực hiện hành động này.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ========================================================================
    // 2. DATA VALIDATION & INTEGRITY ERRORS (400, 404, 409)
    // ========================================================================

    /**
     * Handles bean validation errors triggered by @Valid annotations.
     * <p>
     * Collects all field errors and returns them in a map.
     * HTTP Status: 400 Bad Request
     * </p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        // Iterate over all validation errors and map field names to error messages
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Dữ liệu đầu vào không hợp lệ.")
                .path(request.getRequestURI())
                .validationErrors(errors) // Attach specific field errors
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where a requested resource is not found in the database.
     * <p>
     * HTTP Status: 404 Not Found
     * </p>
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        // Log info level as this is a common operational occurrence
        log.info("Resource not found: {} at {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles database unique constraint violations (e.g., duplicate email).
     * <p>
     * HTTP Status: 409 Conflict
     * </p>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntry(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc dữ liệu.";
        
        // Analyze the exception message to provide a more specific hint (Note: This depends on DB constraint names)
        if (ex.getMessage() != null && ex.getMessage().contains("uk_employees_email")) {
            message = "Email này đã được sử dụng.";
        }

        log.warn("Database conflict: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Conflict")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // ========================================================================
    // 3. GENERIC / SYSTEM ERRORS (500)
    // ========================================================================

    /**
     * Fallback handler for all unexpected exceptions.
     * <p>
     * SECURITY NOTE: This handler logs the full stack trace internally for debugging,
     * but returns a generic error message to the client to prevent information leakage.
     * HTTP Status: 500 Internal Server Error
     * </p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        // Log full stack trace for developers (Critical level)
        log.error("Unexpected Error occurred at API: {}", request.getRequestURI(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Đã xảy ra lỗi hệ thống. Vui lòng liên hệ Admin.") // Generic safe message
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
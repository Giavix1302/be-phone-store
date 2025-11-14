package fit.se.be_phone_store.exception;

import fit.se.be_phone_store.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for all application exceptions
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle custom business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        log.error("Business exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse;
        
        // Check if BadRequestException has data
        if (ex instanceof BadRequestException) {
            BadRequestException badRequestEx = (BadRequestException) ex;
            if (badRequestEx.getData() != null) {
                errorResponse = ErrorResponse.withData(
                    ex.getMessage(),
                    ex.getErrorCode(),
                    ex.getStatusCode(),
                    getPath(request),
                    badRequestEx.getData()
                );
            } else {
                errorResponse = ErrorResponse.of(
                    ex.getMessage(),
                    ex.getErrorCode(),
                    ex.getStatusCode(),
                    getPath(request)
                );
            }
        } else {
            errorResponse = ErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatusCode(),
                getPath(request)
            );
        }
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation exception occurred: {}", ex.getMessage());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.ofValidation(
            "Validation failed",
            fieldErrors,
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle constraint validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint violation exception occurred: {}", ex.getMessage());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        }
        
        ErrorResponse errorResponse = ErrorResponse.ofValidation(
            "Validation failed",
            fieldErrors,
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle Spring Security authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Authentication exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Authentication failed",
            "AUTHENTICATION_FAILED",
            HttpStatus.UNAUTHORIZED.value(),
            getPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle Spring Security access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access denied exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Access denied",
            "ACCESS_DENIED",
            HttpStatus.FORBIDDEN.value(),
            getPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle insufficient stock exceptions with additional details
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex, WebRequest request) {
        log.error("Insufficient stock exception occurred: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getProductId() != null) {
            details.put("productId", ex.getProductId());
        }
        if (ex.getRequestedQuantity() != null) {
            details.put("requestedQuantity", ex.getRequestedQuantity());
        }
        if (ex.getAvailableQuantity() != null) {
            details.put("availableQuantity", ex.getAvailableQuantity());
        }
        
        ErrorResponse errorResponse = ErrorResponse.withDetails(
            ex.getMessage(),
            ex.getErrorCode(),
            ex.getStatusCode(),
            getPath(request),
            details
        );
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    /**
     * Handle method argument type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.error("Method argument type mismatch exception occurred: {}", ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "INVALID_PARAMETER_TYPE",
            HttpStatus.BAD_REQUEST.value(),
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle missing request parameter exceptions
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, WebRequest request) {
        log.error("Missing request parameter exception occurred: {}", ex.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "MISSING_PARAMETER",
            HttpStatus.BAD_REQUEST.value(),
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle missing request part exceptions (for multipart file uploads)
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex, WebRequest request) {
        log.error("Missing request part exception occurred: {}", ex.getMessage());
        
        String partName = ex.getRequestPartName();
        String message;
        if (partName != null && partName.equals("avatar")) {
            message = "Vui lòng chọn file ảnh";
        } else {
            message = String.format("Required part '%s' is not present", partName != null ? partName : "unknown");
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "MISSING_REQUEST_PART",
            HttpStatus.BAD_REQUEST.value(),
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP message not readable exceptions
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("HTTP message not readable exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Invalid request body format",
            "INVALID_REQUEST_BODY",
            HttpStatus.BAD_REQUEST.value(),
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP method not supported exceptions
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.error("HTTP method not supported exception occurred: {}", ex.getMessage());
        
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "METHOD_NOT_SUPPORTED",
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            getPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handle no handler found exceptions
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        log.error("No handler found exception occurred: {}", ex.getMessage());
        
        String message = String.format("Endpoint '%s %s' not found", ex.getHttpMethod(), ex.getRequestURL());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "ENDPOINT_NOT_FOUND",
            HttpStatus.NOT_FOUND.value(),
            getPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "ILLEGAL_ARGUMENT",
            HttpStatus.BAD_REQUEST.value(),
            getPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle null pointer exceptions
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex, WebRequest request) {
        log.error("Null pointer exception occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "An unexpected error occurred",
            "NULL_POINTER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            getPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "An unexpected error occurred. Please contact support if the problem persists.",
            "INTERNAL_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            getPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Extract request path from WebRequest
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}

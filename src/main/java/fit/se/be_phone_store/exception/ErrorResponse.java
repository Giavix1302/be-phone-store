package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ErrorResponse DTO for standardized error responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private Boolean success = false;
    private String message;
    private String errorCode;
    private Integer status;
    private String path;
    private LocalDateTime timestamp;
    
    // For validation errors
    private Map<String, List<String>> fieldErrors;
    
    // For additional error details
    private Map<String, Object> details;

    /**
     * Create a simple error response
     */
    public static ErrorResponse of(String message, int status) {
        return ErrorResponse.builder()
            .success(false)
            .message(message)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create error response with error code
     */
    public static ErrorResponse of(String message, String errorCode, int status) {
        return ErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create error response with path
     */
    public static ErrorResponse of(String message, String errorCode, int status, String path) {
        return ErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .status(status)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create validation error response
     */
    public static ErrorResponse ofValidation(String message, Map<String, List<String>> fieldErrors, String path) {
        return ErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode("VALIDATION_ERROR")
            .status(400)
            .path(path)
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create error response with details
     */
    public static ErrorResponse withDetails(String message, String errorCode, int status, String path, Map<String, Object> details) {
        return ErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .status(status)
            .path(path)
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

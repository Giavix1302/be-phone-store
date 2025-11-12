package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when custom validation fails
 */
public class ValidationException extends BusinessException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";
    
    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = null;
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(Map<String, List<String>> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}

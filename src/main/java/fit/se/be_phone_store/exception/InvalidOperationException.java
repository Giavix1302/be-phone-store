package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to perform an invalid operation
 */
public class InvalidOperationException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_OPERATION";

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}

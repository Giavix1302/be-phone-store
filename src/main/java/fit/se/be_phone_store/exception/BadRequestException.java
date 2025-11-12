package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a request is invalid or malformed
 */
public class BadRequestException extends BusinessException {

    private static final String ERROR_CODE = "BAD_REQUEST";

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
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

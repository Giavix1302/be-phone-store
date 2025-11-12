package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to register with an email that already exists
 */
public class EmailAlreadyExistsException extends BusinessException {

    private static final String ERROR_CODE = "EMAIL_ALREADY_EXISTS";

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' is already registered", email));
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.CONFLICT.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}

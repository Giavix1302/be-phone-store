package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends BusinessException {

    private static final String ERROR_CODE = "AUTHENTICATION_FAILED";

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException() {
        super("Authentication failed");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}

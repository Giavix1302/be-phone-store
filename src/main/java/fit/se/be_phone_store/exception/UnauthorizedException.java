package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user is not authorized to access a resource
 */
public class UnauthorizedException extends BusinessException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException() {
        super("You are not authorized to access this resource");
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

package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user is forbidden from accessing a resource
 */
public class ForbiddenException extends BusinessException {

    private static final String ERROR_CODE = "FORBIDDEN";

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException() {
        super("Access to this resource is forbidden");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.FORBIDDEN.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}

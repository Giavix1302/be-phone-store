package fit.se.be_phone_store.exception;

/**
 * Base exception class for business logic exceptions
 */
public abstract class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get the HTTP status code for this exception
     * @return HTTP status code
     */
    public abstract int getStatusCode();

    /**
     * Get the error code for this exception
     * @return Error code
     */
    public abstract String getErrorCode();
}

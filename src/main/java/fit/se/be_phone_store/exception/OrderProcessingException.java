package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there are issues processing an order
 */
public class OrderProcessingException extends BusinessException {

    private static final String ERROR_CODE = "ORDER_PROCESSING_ERROR";

    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Throwable cause) {
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

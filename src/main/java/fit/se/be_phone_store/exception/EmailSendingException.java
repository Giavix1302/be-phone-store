package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when email sending fails
 */
public class EmailSendingException extends BusinessException {

    private static final String ERROR_CODE = "EMAIL_SENDING_ERROR";

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}


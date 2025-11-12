package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user tries to review a product they have already reviewed
 */
public class ReviewAlreadyExistsException extends BusinessException {

    private static final String ERROR_CODE = "REVIEW_ALREADY_EXISTS";

    public ReviewAlreadyExistsException() {
        super("You have already reviewed this product");
    }

    public ReviewAlreadyExistsException(String message) {
        super(message);
    }

    public ReviewAlreadyExistsException(Long userId, Long productId) {
        super(String.format("User %d has already reviewed product %d", userId, productId));
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

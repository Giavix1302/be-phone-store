package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to access or purchase an inactive product
 */
public class ProductNotActiveException extends BusinessException {

    private static final String ERROR_CODE = "PRODUCT_NOT_ACTIVE";

    public ProductNotActiveException(String message) {
        super(message);
    }

    public ProductNotActiveException(Long productId) {
        super(String.format("Product with ID %d is not active", productId));
    }

    public ProductNotActiveException(String productName) {
        super(String.format("Product '%s' is not available", productName));
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

package fit.se.be_phone_store.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is insufficient stock for a product
 */
public class InsufficientStockException extends BusinessException {

    private static final String ERROR_CODE = "INSUFFICIENT_STOCK";

    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d", 
              productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", 
              productName, requestedQuantity, availableQuantity));
        this.productId = null;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}

package fit.se.be_phone_store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AddToCartRequest DTO for adding items to cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotNull(message = "Product ID is required")
    @JsonProperty("product_id")
    private Long product_id;

    @NotNull(message = "Color ID is required")
    @JsonProperty("color_id")
    private Long color_id;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Backward compatibility getters
    public Long getProductId() {
        return product_id;
    }

    public Long getColorId() {
        return color_id;
    }
}
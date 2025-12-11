package fit.se.be_phone_store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CreateOrderRequest DTO for creating new orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    @JsonProperty("shipping_address")
    private String shipping_address;

    @JsonProperty("payment_method")
    private String payment_method = "COD";

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String note;


    @JsonProperty("cart_item_ids")
    private List<Long> cart_item_ids;

    @JsonProperty("buy_now_items")
    private List<BuyNowItem> buy_now_items;

    // Inner class for buy now items
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyNowItem {
        @JsonProperty("product_id")
        private Long product_id;

        @JsonProperty("color_id")
        private Long color_id;

        @JsonProperty("quantity")
        private Integer quantity;
    }

    // Backward compatibility
    public String getShippingAddress() {
        return shipping_address;
    }

    public String getPaymentMethod() {
        return payment_method;
    }

    public String getNotes() {
        return note;
    }

    public List<Long> getCartItemIds() {
        return cart_item_ids;
    }

    public List<BuyNowItem> getBuyNowItems() {
        return buy_now_items;
    }
}
package fit.se.be_phone_store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SyncCartRequest DTO for syncing guest cart to authenticated user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncCartRequest {

    @NotNull(message = "Guest cart items are required")
    @Valid
    private List<GuestCartItem> guest_cart_items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestCartItem {
        @NotNull(message = "Product ID is required")
        private Long product_id;

        @NotNull(message = "Color ID is required")
        private Long color_id;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}


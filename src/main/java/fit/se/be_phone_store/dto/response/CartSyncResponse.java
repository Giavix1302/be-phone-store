package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CartSyncResponse DTO for cart sync result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSyncResponse {

    @JsonProperty("synced_items")
    private Integer synced_items;

    @JsonProperty("merged_items")
    private Integer merged_items;

    @JsonProperty("new_items")
    private Integer new_items;

    @JsonProperty("failed_items")
    private Integer failed_items;

    @JsonProperty("cart_summary")
    private CartSummary cart_summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartSummary {
        @JsonProperty("total_items")
        private Integer total_items;

        @JsonProperty("total_quantity")
        private Integer total_quantity;
    }
}


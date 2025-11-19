package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CartValidateResponse DTO for cart validation result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartValidateResponse {

    @JsonProperty("is_valid")
    private Boolean is_valid;

    @JsonProperty("total_items")
    private Integer total_items;

    @JsonProperty("available_items")
    private Integer available_items;

    @JsonProperty("unavailable_items")
    private Integer unavailable_items;

    @JsonProperty("price_changes")
    private List<PriceChange> price_changes;

    @JsonProperty("stock_issues")
    private List<StockIssue> stock_issues;

    @JsonProperty("unavailable_products")
    private List<UnavailableProduct> unavailable_products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceChange {
        @JsonProperty("item_id")
        private Long item_id;

        @JsonProperty("product_name")
        private String product_name;

        @JsonProperty("old_price")
        private BigDecimal old_price;

        @JsonProperty("new_price")
        private BigDecimal new_price;
        private Boolean updated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockIssue {
        @JsonProperty("item_id")
        private Long item_id;

        @JsonProperty("product_name")
        private String product_name;

        @JsonProperty("requested_quantity")
        private Integer requested_quantity;

        @JsonProperty("available_quantity")
        private Integer available_quantity;
        private String issue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnavailableProduct {
        @JsonProperty("item_id")
        private Long item_id;

        @JsonProperty("product_name")
        private String product_name;
        private String issue;
    }
}


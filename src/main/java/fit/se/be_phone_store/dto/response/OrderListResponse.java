package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderListResponse DTO for order list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderItem> orders;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long id;

        @JsonProperty("order_number")
        private String order_number;

        @JsonProperty("total_amount")
        private BigDecimal total_amount;
        private String status;

        @JsonProperty("payment_method")
        private String payment_method;

        @JsonProperty("items_count")
        private Integer items_count;

        @JsonProperty("items_preview")
        private List<ItemPreview> items_preview;

        @JsonProperty("created_at")
        private LocalDateTime created_at;

        @JsonProperty("delivered_at")
        private LocalDateTime delivered_at;

        @JsonProperty("can_cancel")
        private Boolean can_cancel;

        @JsonProperty("can_review")
        private Boolean can_review;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPreview {
        @JsonProperty("product_name")
        private String product_name;

        @JsonProperty("product_image")
        private String product_image;
        private Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        @JsonProperty("current_page")
        private Integer current_page;

        @JsonProperty("total_pages")
        private Integer total_pages;

        @JsonProperty("total_items")
        private Long total_items;

        @JsonProperty("items_per_page")
        private Integer items_per_page;

        @JsonProperty("has_next")
        private Boolean has_next;

        @JsonProperty("has_prev")
        private Boolean has_prev;
    }
}


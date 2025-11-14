package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AdminOrderListResponse DTO for admin order list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderListResponse {

    private List<AdminOrderItem> orders;
    private PaginationInfo pagination;
    private SummaryInfo summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminOrderItem {
        private Long id;

        @JsonProperty("order_number")
        private String order_number;
        private UserInfo user;

        @JsonProperty("total_amount")
        private BigDecimal total_amount;
        private String status;

        @JsonProperty("payment_method")
        private String payment_method;

        @JsonProperty("items_count")
        private Integer items_count;

        @JsonProperty("shipping_address")
        private String shipping_address;

        @JsonProperty("created_at")
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;

        @JsonProperty("full_name")
        private String full_name;
        private String email;
        private String phone;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryInfo {
        @JsonProperty("total_orders")
        private Integer total_orders;

        @JsonProperty("pending_orders")
        private Integer pending_orders;

        @JsonProperty("processing_orders")
        private Integer processing_orders;

        @JsonProperty("shipped_orders")
        private Integer shipped_orders;

        @JsonProperty("delivered_orders")
        private Integer delivered_orders;

        @JsonProperty("cancelled_orders")
        private Integer cancelled_orders;

        @JsonProperty("total_revenue")
        private BigDecimal total_revenue;
    }
}


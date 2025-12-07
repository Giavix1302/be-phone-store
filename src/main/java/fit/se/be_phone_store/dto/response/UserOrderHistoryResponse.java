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
 * UserOrderHistoryResponse - Response DTO for user order history (Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderHistoryResponse {

    @JsonProperty("user")
    private UserInfo user;

    @JsonProperty("orders")
    private List<OrderInfo> orders;

    @JsonProperty("pagination")
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("email")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("order_number")
        private String orderNumber;

        @JsonProperty("total_amount")
        private BigDecimal totalAmount;

        @JsonProperty("status")
        private String status;

        @JsonProperty("items_count")
        private Integer itemsCount;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        @JsonProperty("current_page")
        private Integer currentPage;

        @JsonProperty("total_pages")
        private Integer totalPages;

        @JsonProperty("total_items")
        private Long totalItems;

        @JsonProperty("items_per_page")
        private Integer itemsPerPage;
    }
}


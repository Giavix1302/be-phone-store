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
 * AdminUserDetailResponse - Response DTO for admin user detail endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {

    @JsonProperty("user_info")
    private UserInfo userInfo;

    @JsonProperty("statistics")
    private StatisticsInfo statistics;

    @JsonProperty("recent_orders")
    private List<RecentOrder> recentOrders;

    @JsonProperty("recent_reviews")
    private List<RecentReview> recentReviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("email")
        private String email;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("address")
        private String address;

        @JsonProperty("avatar")
        private String avatar;

        @JsonProperty("role")
        private String role;

        @JsonProperty("enabled")
        private Boolean enabled;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        @JsonProperty("last_login")
        private LocalDateTime lastLogin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsInfo {
        @JsonProperty("total_orders")
        private Integer totalOrders;

        @JsonProperty("completed_orders")
        private Integer completedOrders;

        @JsonProperty("cancelled_orders")
        private Integer cancelledOrders;

        @JsonProperty("total_spent")
        private BigDecimal totalSpent;

        @JsonProperty("average_order_value")
        private BigDecimal averageOrderValue;

        @JsonProperty("total_reviews")
        private Integer totalReviews;

        @JsonProperty("average_rating")
        private Double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("order_number")
        private String orderNumber;

        @JsonProperty("total_amount")
        private BigDecimal totalAmount;

        @JsonProperty("status")
        private String status;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentReview {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("rating")
        private Integer rating;

        @JsonProperty("comment")
        private String comment;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }
}


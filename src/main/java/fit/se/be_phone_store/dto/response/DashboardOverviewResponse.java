package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DashboardOverviewResponse - Response DTO for admin dashboard overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {

    @JsonProperty("period")
    private String period;

    @JsonProperty("overview")
    private OverviewInfo overview;

    @JsonProperty("today_stats")
    private TodayStatsInfo todayStats;

    @JsonProperty("comparisons")
    private ComparisonsInfo comparisons;

    @JsonProperty("quick_stats")
    private QuickStatsInfo quickStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewInfo {
        @JsonProperty("total_revenue")
        private BigDecimal totalRevenue;

        @JsonProperty("total_orders")
        private Long totalOrders;

        @JsonProperty("total_users")
        private Long totalUsers;

        @JsonProperty("total_products")
        private Long totalProducts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayStatsInfo {
        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("orders")
        private Long orders;

        @JsonProperty("new_users")
        private Long newUsers;

        @JsonProperty("active_users")
        private Long activeUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonsInfo {
        @JsonProperty("revenue_growth")
        private Double revenueGrowth;

        @JsonProperty("orders_growth")
        private Double ordersGrowth;

        @JsonProperty("users_growth")
        private Double usersGrowth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickStatsInfo {
        @JsonProperty("pending_orders")
        private Long pendingOrders;

        @JsonProperty("low_stock_products")
        private Long lowStockProducts;

        @JsonProperty("recent_reviews")
        private Long recentReviews;

        @JsonProperty("active_users_today")
        private Long activeUsersToday;
    }
}


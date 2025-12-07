package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * RevenueAnalyticsResponse - Response DTO for revenue analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {

    @JsonProperty("period")
    private String period;

    @JsonProperty("revenue_summary")
    private RevenueSummaryInfo revenueSummary;

    @JsonProperty("daily_revenue")
    private List<DailyRevenue> dailyRevenue;

    @JsonProperty("revenue_by_category")
    private List<RevenueByCategory> revenueByCategory;

    @JsonProperty("top_revenue_products")
    private List<TopRevenueProduct> topRevenueProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSummaryInfo {
        @JsonProperty("total_revenue")
        private BigDecimal totalRevenue;

        @JsonProperty("revenue_growth")
        private Double revenueGrowth;

        @JsonProperty("average_order_value")
        private BigDecimal averageOrderValue;

        @JsonProperty("total_orders")
        private Long totalOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenue {
        @JsonProperty("date")
        private String date;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("orders")
        private Integer orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByCategory {
        @JsonProperty("category")
        private String category;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRevenueProduct {
        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("quantity_sold")
        private Integer quantitySold;
    }
}


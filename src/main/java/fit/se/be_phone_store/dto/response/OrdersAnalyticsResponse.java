package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * OrdersAnalyticsResponse - Response DTO for orders analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersAnalyticsResponse {

    @JsonProperty("period")
    private String period;

    @JsonProperty("orders_summary")
    private OrdersSummaryInfo ordersSummary;

    @JsonProperty("daily_orders")
    private List<DailyOrder> dailyOrders;

    @JsonProperty("orders_by_status")
    private Map<String, Integer> ordersByStatus;

    @JsonProperty("peak_hours")
    private List<PeakHour> peakHours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdersSummaryInfo {
        @JsonProperty("total_orders")
        private Long totalOrders;

        @JsonProperty("completed_orders")
        private Long completedOrders;

        @JsonProperty("cancelled_orders")
        private Long cancelledOrders;

        @JsonProperty("completion_rate")
        private Double completionRate;

        @JsonProperty("average_order_value")
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyOrder {
        @JsonProperty("date")
        private String date;

        @JsonProperty("orders")
        private Integer orders;

        @JsonProperty("completed")
        private Integer completed;

        @JsonProperty("cancelled")
        private Integer cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeakHour {
        @JsonProperty("hour")
        private Integer hour;

        @JsonProperty("orders")
        private Integer orders;
    }
}


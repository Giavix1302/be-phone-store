package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * OrderStatisticsResponse DTO for order statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {

    private String period;

    @JsonProperty("from_date")
    private LocalDate from_date;

    @JsonProperty("to_date")
    private LocalDate to_date;

    private OverviewInfo overview;

    @JsonProperty("status_breakdown")
    private Map<String, Integer> status_breakdown;

    @JsonProperty("daily_stats")
    private List<DailyStat> daily_stats;

    @JsonProperty("top_products")
    private List<TopProduct> top_products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewInfo {
        @JsonProperty("total_orders")
        private Integer total_orders;

        @JsonProperty("total_revenue")
        private BigDecimal total_revenue;

        @JsonProperty("average_order_value")
        private BigDecimal average_order_value;

        @JsonProperty("completion_rate")
        private Double completion_rate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private LocalDate date;

        @JsonProperty("orders_count")
        private Integer orders_count;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        @JsonProperty("product_id")
        private Long product_id;

        @JsonProperty("product_name")
        private String product_name;

        @JsonProperty("quantity_sold")
        private Integer quantity_sold;
        private BigDecimal revenue;
    }
}


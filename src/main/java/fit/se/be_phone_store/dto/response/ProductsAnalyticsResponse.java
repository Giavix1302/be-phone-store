package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductsAnalyticsResponse - Response DTO for products analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsAnalyticsResponse {

    @JsonProperty("products_summary")
    private ProductsSummaryInfo productsSummary;

    @JsonProperty("best_selling_products")
    private List<BestSellingProduct> bestSellingProducts;

    @JsonProperty("low_stock_alerts")
    private List<LowStockAlert> lowStockAlerts;

    @JsonProperty("category_performance")
    private List<CategoryPerformance> categoryPerformance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductsSummaryInfo {
        @JsonProperty("total_products")
        private Long totalProducts;

        @JsonProperty("active_products")
        private Long activeProducts;

        @JsonProperty("inactive_products")
        private Long inactiveProducts;

        @JsonProperty("low_stock_products")
        private Long lowStockProducts;

        @JsonProperty("out_of_stock_products")
        private Long outOfStockProducts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestSellingProduct {
        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("quantity_sold")
        private Integer quantitySold;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("stock_remaining")
        private Integer stockRemaining;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlert {
        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("current_stock")
        private Integer currentStock;

        @JsonProperty("recommended_reorder")
        private Integer recommendedReorder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        @JsonProperty("category")
        private String category;

        @JsonProperty("total_products")
        private Long totalProducts;

        @JsonProperty("products_sold")
        private Long productsSold;

        @JsonProperty("revenue")
        private BigDecimal revenue;
    }
}


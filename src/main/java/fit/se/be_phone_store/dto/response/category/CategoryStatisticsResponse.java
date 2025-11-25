package fit.se.be_phone_store.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CategoryStatisticsResponse DTO for detailed category statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatisticsResponse {

    private CategoryInfo category;

    @JsonProperty("product_statistics")
    private ProductStatistics productStatistics;

    @JsonProperty("sales_statistics")
    private SalesStatistics salesStatistics;

    @JsonProperty("brand_breakdown")
    private List<BrandBreakdown> brandBreakdown;

    @JsonProperty("price_range")
    private PriceRange priceRange;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStatistics {
        @JsonProperty("total_products")
        private Integer totalProducts;

        @JsonProperty("active_products")
        private Integer activeProducts;

        @JsonProperty("inactive_products")
        private Integer inactiveProducts;

        @JsonProperty("products_in_stock")
        private Integer productsInStock;

        @JsonProperty("products_out_of_stock")
        private Integer productsOutOfStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesStatistics {
        @JsonProperty("total_orders")
        private Long totalOrders;

        @JsonProperty("total_revenue")
        private BigDecimal totalRevenue;

        @JsonProperty("average_product_price")
        private BigDecimal averageProductPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandBreakdown {
        @JsonProperty("brand_id")
        private Long brandId;

        @JsonProperty("brand_name")
        private String brandName;

        @JsonProperty("product_count")
        private Integer productCount;

        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRange {
        @JsonProperty("min_price")
        private BigDecimal minPrice;

        @JsonProperty("max_price")
        private BigDecimal maxPrice;

        @JsonProperty("average_price")
        private BigDecimal averagePrice;
    }
}
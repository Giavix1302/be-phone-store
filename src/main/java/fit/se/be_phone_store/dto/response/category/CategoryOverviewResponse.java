package fit.se.be_phone_store.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CategoryOverviewResponse DTO for categories overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryOverviewResponse {

    @JsonProperty("total_categories")
    private Integer totalCategories;

    @JsonProperty("total_products")
    private Integer totalProducts;

    @JsonProperty("categories_summary")
    private List<CategorySummary> categoriesSummary;

    private Trends trends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private Long id;
        private String name;

        @JsonProperty("product_count")
        private Integer productCount;

        @JsonProperty("revenue_percentage")
        private Double revenuePercentage;

        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trends {
        @JsonProperty("fastest_growing")
        private FastestGrowing fastestGrowing;

        @JsonProperty("most_products")
        private MostProducts mostProducts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FastestGrowing {
        @JsonProperty("category_id")
        private Long categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("growth_rate")
        private Double growthRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostProducts {
        @JsonProperty("category_id")
        private Long categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("product_count")
        private Integer productCount;
    }
}
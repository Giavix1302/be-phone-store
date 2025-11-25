package fit.se.be_phone_store.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
/**
 * CategoryResponse DTO for category data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("product_count")
    private Integer productCount;

    @JsonProperty("active_product_count")
    private Integer activeProductCount;

    @JsonProperty("inactive_product_count")
    private Integer inactiveProductCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Additional fields for admin/statistics use cases
    @JsonProperty("revenue_percentage")
    private Double revenuePercentage;

    private BigDecimal revenue;

    @JsonProperty("products_in_stock")
    private Integer productsInStock;

    @JsonProperty("products_out_of_stock")
    private Integer productsOutOfStock;
}
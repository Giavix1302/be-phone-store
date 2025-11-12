package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductResponse DTO for product list data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private Boolean isActive;
    private String categoryName;
    private String brandName;
    private String colorName;
    private String mainImageUrl;
    private Boolean hasDiscount;
    private Boolean inStock;
    private LocalDateTime createdAt;
}

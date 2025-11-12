package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ProductDetailResponse DTO for detailed product information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private Boolean isActive;
    
    private Map<String, Object> category;
    private Map<String, Object> brand;
    private Map<String, Object> defaultColor;
    
    private List<Map<String, Object>> availableColors;
    private List<Map<String, String>> specifications;
    private List<Map<String, Object>> images;
    
    private Double averageRating;
    private Integer reviewCount;
    private Boolean hasDiscount;
    private Boolean inStock;
    private LocalDateTime createdAt;
}

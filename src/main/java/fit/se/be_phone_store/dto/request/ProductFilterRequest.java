package fit.se.be_phone_store.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ProductFilterRequest DTO for filtering products with multiple criteria
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    private Long categoryId;

    private Long brandId;

    private Long colorId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Boolean inStock;

    private String keyword;

    private String sortBy = "createdAt";

    private String sortDirection = "desc";
}

package fit.se.be_phone_store.dto.response.color;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ColorUsageResponse DTO for color usage information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorUsageResponse {

    private ColorResponse color;
    private UsageSummary usageSummary;
    private List<ProductInfo> productsUsingAsDefault;
    private List<ProductInfo> productsUsingAsAvailable;
    private Boolean canDelete;
    private String deleteBlockedReason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageSummary {
        private Long totalProducts;
        private Long asDefaultColor;
        private Long asAvailableColor;
        private Long totalUsage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
    }
}

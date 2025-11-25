package fit.se.be_phone_store.dto.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductResponse DTO for product data
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

    @JsonProperty("discount_price")
    private BigDecimal discountPrice;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    private CategoryInfo category;
    private BrandInfo brand;

    @JsonProperty("default_color")
    private ColorInfo defaultColor;

    @JsonProperty("primary_image")
    private String primaryImage;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Additional fields for detailed responses
    @JsonProperty("available_colors")
    private List<ColorInfo> availableColors;

    private List<ImageInfo> images;
    private List<SpecificationInfo> specifications;
    private List<ReviewInfo> reviews;

    @JsonProperty("average_rating")
    private Double averageRating;

    @JsonProperty("total_reviews")
    private Integer totalReviews;

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
    public static class BrandInfo {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorInfo {
        private Long id;

        @JsonProperty("color_name")
        private String colorName;

        @JsonProperty("hex_code")
        private String hexCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private Long id;

        @JsonProperty("image_url")
        private String imageUrl;

        @JsonProperty("alt_text")
        private String altText;

        @JsonProperty("is_primary")
        private Boolean isPrimary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecificationInfo {
        private Long id;

        @JsonProperty("spec_name")
        private String specName;

        @JsonProperty("spec_value")
        private String specValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInfo {
        private Long id;
        private UserInfo user;
        private Integer rating;
        private String comment;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;

        @JsonProperty("full_name")
        private String fullName;
    }
}
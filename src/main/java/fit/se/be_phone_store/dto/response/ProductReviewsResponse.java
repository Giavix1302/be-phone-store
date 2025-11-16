package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for Product Reviews API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewsResponse {
    
    private ProductInfo product;
    private ReviewsSummary reviewsSummary;
    private List<ReviewItem> reviews;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewsSummary {
        private Long totalReviews;
        private Double averageRating;
        private Map<String, Long> ratingBreakdown;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItem {
        private Long id;
        private UserInfo user;
        private Integer rating;
        private String comment;
        private String createdAt;
        private String updatedAt;
        private Boolean isVerifiedPurchase;
        private Boolean isOwnReview;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String avatar;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalItems;
        private Integer itemsPerPage;
        private Boolean hasNext;
        private Boolean hasPrev;
    }
}


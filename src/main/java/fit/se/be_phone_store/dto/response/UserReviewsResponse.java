package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReviewsResponse {

    private List<ReviewItem> reviews;
    private PaginationInfo pagination;
    private ReviewsSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItem {
        private Long id;
        private ProductInfo product;
        private Integer rating;
        private String comment;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
        @JsonProperty("primary_image")
        private String primaryImage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        @JsonProperty("current_page")
        private Integer currentPage;
        @JsonProperty("total_pages")
        private Integer totalPages;
        @JsonProperty("total_items")
        private Long totalItems;
        @JsonProperty("items_per_page")
        private Integer itemsPerPage;
        @JsonProperty("has_next")
        private Boolean hasNext;
        @JsonProperty("has_prev")
        private Boolean hasPrev;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewsSummary {
        @JsonProperty("total_reviews")
        private Long totalReviews;
        @JsonProperty("average_rating")
        private Double averageRating;
    }
}


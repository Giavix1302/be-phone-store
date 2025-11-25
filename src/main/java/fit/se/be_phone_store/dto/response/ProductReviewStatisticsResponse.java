package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for product-specific review statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewStatisticsResponse {

    private ProductInfo product;
    private Overview overview;
    private Map<String, RatingStat> ratingBreakdown;
    private RecentTrends recentTrends;

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
    public static class Overview {
        private long totalReviews;
        private double averageRating;
        private double recommendationPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingStat {
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTrends {
        private Last30Days last30Days;
        private String ratingTrend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Last30Days {
        private long newReviews;
        private double averageRating;
    }
}



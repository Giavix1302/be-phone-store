package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO representing admin review statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewStatisticsResponse {

    private String period;
    private String fromDate;
    private String toDate;
    private Overview overview;
    private Map<String, Long> ratingDistribution;
    private List<DailyStat> dailyStats;
    private List<TopRatedProduct> topRatedProducts;
    private List<MostActiveReviewer> mostActiveReviewers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private long totalReviews;
        private double averageRating;
        private long totalProductsReviewed;
        private long totalReviewers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private String date;
        private long reviewsCount;
        private double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRatedProduct {
        private Long productId;
        private String productName;
        private double averageRating;
        private long totalReviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostActiveReviewer {
        private Long userId;
        private String userName;
        private long totalReviews;
        private double averageRating;
    }
}



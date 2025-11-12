package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ReviewStatisticsResponse DTO for review statistics data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsResponse {

    private Long productId;
    private Integer totalReviews;
    private Double averageRating;
    private Map<Integer, Long> ratingDistribution; // Rating (1-5) -> Count
}

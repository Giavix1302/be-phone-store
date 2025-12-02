package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for submitting order review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitOrderReviewResponse {

    private String orderNumber;
    private List<ReviewedProduct> reviewedProducts;
    private String reviewedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewedProduct {
        private Long productId;
        private String productName;
        private Integer rating;
        private Long reviewId;
        private String status; 
        private String message; 
    }
}



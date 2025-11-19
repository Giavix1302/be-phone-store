package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Create Review API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewResponse {
    
    private ReviewData review;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewData {
        private Long id;
        private UserInfo user;
        private ProductInfo product;
        private Integer rating;
        private String comment;
        
        @com.fasterxml.jackson.annotation.JsonProperty("created_at")
        private String createdAt;
        
        @com.fasterxml.jackson.annotation.JsonProperty("is_verified_purchase")
        private Boolean isVerifiedPurchase;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        
        @com.fasterxml.jackson.annotation.JsonProperty("full_name")
        private String fullName;
        private String avatar;
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


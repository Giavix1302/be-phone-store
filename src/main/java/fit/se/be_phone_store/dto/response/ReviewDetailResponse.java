package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponse {

    private Long id;
    private UserInfo user;
    private ProductInfo product;
    private Integer rating;
    private String comment;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("is_verified_purchase")
    private Boolean isVerifiedPurchase;
    @JsonProperty("purchase_date")
    private String purchaseDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        @JsonProperty("full_name")
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
        @JsonProperty("primary_image")
        private String primaryImage;
    }
}


package fit.se.be_phone_store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SubmitOrderReviewRequest DTO for submitting order reviews
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitOrderReviewRequest {

    @NotNull(message = "Reviews are required")
    @Valid
    private List<ProductReview> reviews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductReview {
        @NotNull(message = "Product ID is required")
        @JsonProperty("product_id")
        private Long product_id;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        private Integer rating;

        @NotNull(message = "Comment is required")
        @Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
        private String comment;
    }
}


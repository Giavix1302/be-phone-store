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
public class AdminDeleteReviewResponse {

    @JsonProperty("deleted_review_id")
    private Long deletedReviewId;
    private String reason;
    @JsonProperty("deleted_at")
    private String deletedAt;
    @JsonProperty("deleted_by")
    private DeletedByInfo deletedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeletedByInfo {
        private Long id;
        @JsonProperty("full_name")
        private String fullName;
    }
}


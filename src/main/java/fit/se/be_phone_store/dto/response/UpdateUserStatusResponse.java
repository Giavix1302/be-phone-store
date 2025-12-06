package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UpdateUserStatusResponse - Response DTO for user status update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("old_status")
    private Boolean oldStatus;

    @JsonProperty("new_status")
    private Boolean newStatus;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("updated_by")
    private UpdatedByInfo updatedBy;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatedByInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("full_name")
        private String fullName;
    }
}


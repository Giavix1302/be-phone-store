package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * AvatarResponse DTO - Response cho upload/remove avatar
 */
public class AvatarResponse {

    @JsonProperty("user_id")
    private Long userId;

    private String avatar;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public AvatarResponse() {}

    public AvatarResponse(Long userId, String avatar, LocalDateTime updatedAt) {
        this.userId = userId;
        this.avatar = avatar;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


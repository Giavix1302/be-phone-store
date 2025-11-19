package fit.se.be_phone_store.dto.response;

/**
 * UpdateAvatarResponse DTO - Response cho PATCH /api/users/me/avatar
 */
public class UpdateAvatarResponse {

    private String avatar;

    // Constructors
    public UpdateAvatarResponse() {}

    public UpdateAvatarResponse(String avatar) {
        this.avatar = avatar;
    }

    // Getters and Setters
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}


package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ChangePasswordRequest DTO for changing password
 */
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String old_password;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String new_password;

    // Constructors
    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String old_password, String new_password) {
        this.old_password = old_password;
        this.new_password = new_password;
    }

    // Getters and Setters
    public String getOld_password() {
        return old_password;
    }

    public void setOld_password(String old_password) {
        this.old_password = old_password;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }
}

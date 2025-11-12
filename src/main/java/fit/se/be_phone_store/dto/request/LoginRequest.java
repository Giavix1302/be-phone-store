package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest DTO for user login - NO LOMBOK DEPENDENCY
 */
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Explicit Getter Methods
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setter Methods
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
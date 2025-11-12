package fit.se.be_phone_store.dto.response;

/**
 * LoginResponse DTO for login response - NO LOMBOK DEPENDENCY
 */
public class LoginResponse {

    private String token;
    private String type;
    private String username;
    private String email;
    private String fullName;
    private String role;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String token, String type, String username, String email, String fullName, String role) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // Builder Pattern Implementation (Manual)
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String token;
        private String type;
        private String username;
        private String email;
        private String fullName;
        private String role;

        public LoginResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public LoginResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LoginResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public LoginResponseBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public LoginResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(token, type, username, email, fullName, role);
        }
    }

    // Getter Methods
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    // Setter Methods
    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
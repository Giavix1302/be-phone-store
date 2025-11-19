package fit.se.be_phone_store.dto.response;

/**
 * LoginResponse DTO for login response - NO LOMBOK DEPENDENCY
 */
public class LoginResponse {

    private String access_token;
    private UserInfo user;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String access_token, UserInfo user) {
        this.access_token = access_token;
        this.user = user;
    }

    // Builder Pattern Implementation (Manual)
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String access_token;
        private UserInfo user;

        public LoginResponseBuilder access_token(String access_token) {
            this.access_token = access_token;
            return this;
        }

        public LoginResponseBuilder user(UserInfo user) {
            this.user = user;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(access_token, user);
        }
    }

   
    public String getAccess_token() {
        return access_token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        private Long id;
        private String email;
        private String full_name;
        private String phone;
        private String role;

        // Constructors
        public UserInfo() {}

        public UserInfo(Long id, String email, String full_name, String phone, String role) {
            this.id = id;
            this.email = email;
            this.full_name = full_name;
            this.phone = phone;
            this.role = role;
        }

        // Builder Pattern
        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        public static class UserInfoBuilder {
            private Long id;
            private String email;
            private String full_name;
            private String phone;
            private String role;

            public UserInfoBuilder id(Long id) {
                this.id = id;
                return this;
            }

            public UserInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public UserInfoBuilder full_name(String full_name) {
                this.full_name = full_name;
                return this;
            }

            public UserInfoBuilder phone(String phone) {
                this.phone = phone;
                return this;
            }

            public UserInfoBuilder role(String role) {
                this.role = role;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(id, email, full_name, phone, role);
            }
        }

        // Getters
        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFull_name() {
            return full_name;
        }

        public String getPhone() {
            return phone;
        }

        public String getRole() {
            return role;
        }

        // Setters
        public void setId(Long id) {
            this.id = id;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setFull_name(String full_name) {
            this.full_name = full_name;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

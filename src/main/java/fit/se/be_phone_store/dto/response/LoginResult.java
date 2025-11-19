package fit.se.be_phone_store.dto.response;


public class LoginResult {
    private LoginResponse loginResponse;
    private String refreshToken;

    public LoginResult() {}

    public LoginResult(LoginResponse loginResponse, String refreshToken) {
        this.loginResponse = loginResponse;
        this.refreshToken = refreshToken;
    }

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }

    public void setLoginResponse(LoginResponse loginResponse) {
        this.loginResponse = loginResponse;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}


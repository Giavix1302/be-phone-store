package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class VerifyEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be 6 digits")
    private String verification_code;

    public VerifyEmailRequest() {}

    public VerifyEmailRequest(String email, String verification_code) {
        this.email = email;
        this.verification_code = verification_code;
    }

    public String getEmail() {
        return email;
    }

    public String getVerification_code() {
        return verification_code;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVerification_code(String verification_code) {
        this.verification_code = verification_code;
    }
}


package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * ValidateProfileRequest DTO for validating profile data - NO LOMBOK DEPENDENCY
 */
public class ValidateProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String full_name;

    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone must have 10 digits and start with 0")
    private String phone;

    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters if provided")
    private String address;

    // Constructors
    public ValidateProfileRequest() {}

    public ValidateProfileRequest(String full_name, String phone, String address) {
        this.full_name = full_name;
        this.phone = phone;
        this.address = address;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}


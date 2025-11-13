package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String full_name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone must have 10 digits and start with 0")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    // Constructors
    public SignupRequest() {}

    public SignupRequest(String email, String password, String full_name, String phone, String address) {
        this.email = email;
        this.password = password;
        this.full_name = full_name;
        this.phone = phone;
        this.address = address;
    }

    // Explicit Getter Methods
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    // Setter Methods
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
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


package fit.se.be_phone_store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateUserStatusRequest DTO for updating user enabled status (Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "Enabled status is required")
    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("reason")
    private String reason;
}


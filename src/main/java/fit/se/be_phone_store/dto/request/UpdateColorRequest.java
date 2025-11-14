package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateColorRequest DTO for updating cart item color
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColorRequest {

    @NotNull(message = "Color ID is required")
    private Long color_id;
}


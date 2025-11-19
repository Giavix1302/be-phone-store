package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateQuantityRequest DTO for updating cart item quantity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or greater")
    private Integer quantity;
}


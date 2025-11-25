package fit.se.be_phone_store.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateStockRequest DTO for updating product stock
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    @Pattern(regexp = "^(set|add|subtract)$", message = "Operation phải là: set, add, hoặc subtract")
    private String operation = "set";
}
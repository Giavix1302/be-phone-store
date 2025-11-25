package fit.se.be_phone_store.dto.request.product;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ManageProductColorsRequest DTO for adding/removing colors to/from products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManageProductColorsRequest {

    @NotEmpty(message = "Danh sách màu không được để trống")
    private List<Long> colorIds;
}
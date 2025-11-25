package fit.se.be_phone_store.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateSpecificationRequest DTO for updating individual specification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSpecificationRequest {

    @NotBlank(message = "Tên thông số không được để trống")
    @Size(max = 100, message = "Tên thông số không được vượt quá 100 ký tự")
    private String specName;

    @NotBlank(message = "Giá trị thông số không được để trống")
    @Size(max = 255, message = "Giá trị thông số không được vượt quá 255 ký tự")
    private String specValue;
}
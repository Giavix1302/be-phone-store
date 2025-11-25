package fit.se.be_phone_store.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateCategoryRequest DTO for updating categories
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2-100 ký tự")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$",
            message = "Tên danh mục phải bắt đầu bằng chữ cái viết hoa, chỉ chứa chữ cái viết hoa, số và dấu gạch dưới")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;
}
package fit.se.be_phone_store.dto.request.color;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateColorRequest DTO for creating new color
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateColorRequest {

    @NotBlank(message = "Tên màu không được để trống")
    @Size(max = 50, message = "Tên màu không được vượt quá 50 ký tự")
    private String colorName;

    @NotBlank(message = "Mã màu hex không được để trống")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu hex không đúng định dạng. Vui lòng sử dụng format #RRGGBB")
    private String hexCode;
}
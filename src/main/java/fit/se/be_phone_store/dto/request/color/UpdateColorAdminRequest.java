package fit.se.be_phone_store.dto.request.color;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColorAdminRequest {

    @Size(max = 50, message = "Tên màu không được vượt quá 50 ký tự")
    private String colorName;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu hex không đúng định dạng. Vui lòng sử dụng format #RRGGBB")
    private String hexCode;
}
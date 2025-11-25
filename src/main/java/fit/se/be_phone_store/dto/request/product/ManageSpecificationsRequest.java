package fit.se.be_phone_store.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ManageSpecificationsRequest DTO for adding specifications to products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManageSpecificationsRequest {

    @NotEmpty(message = "Danh sách thông số không được để trống")
    @Valid
    private List<SpecificationItem> specifications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecificationItem {

        @NotBlank(message = "Tên thông số không được để trống")
        @Size(max = 100, message = "Tên thông số không được vượt quá 100 ký tự")
        private String specName;

        @NotBlank(message = "Giá trị thông số không được để trống")
        @Size(max = 255, message = "Giá trị thông số không được vượt quá 255 ký tự")
        private String specValue;
    }
}
package fit.se.be_phone_store.dto.request.product;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateImageRequest DTO for updating product image properties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImageRequest {

    @Size(max = 255, message = "Alt text không được vượt quá 255 ký tự")
    private String altText;

    private Boolean isPrimary;
}
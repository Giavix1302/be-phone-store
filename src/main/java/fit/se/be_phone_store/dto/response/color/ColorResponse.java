package fit.se.be_phone_store.dto.response.color;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ColorResponse DTO for color data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorResponse {

    private Long id;
    private String colorName;
    private String hexCode;
}

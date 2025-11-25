package fit.se.be_phone_store.dto.response.color;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ColorDetailResponse DTO for color detail information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorDetailResponse {

    private Long id;

    private String colorName;

    private String hexCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}

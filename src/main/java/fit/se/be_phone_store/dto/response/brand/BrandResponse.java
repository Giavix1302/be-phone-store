package fit.se.be_phone_store.dto.response.brand;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * BrandResponse DTO for brand data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("product_count")
    private Integer productCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Additional field for admin responses
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
package fit.se.be_phone_store.dto.response.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeletedCategoryResponse DTO for category deletion response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedCategoryResponse {

    @JsonProperty("deleted_category_id")
    private Long deletedCategoryId;

    private String message;
}

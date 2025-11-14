package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CartCountResponse DTO for cart count information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartCountResponse {

    @JsonProperty("total_items")
    private Integer total_items;

    @JsonProperty("total_quantity")
    private Integer total_quantity;

    @JsonProperty("available_items")
    private Integer available_items;

    @JsonProperty("unavailable_items")
    private Integer unavailable_items;
}


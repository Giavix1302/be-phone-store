package fit.se.be_phone_store.dto.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StockUpdateResponse DTO for stock update responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateResponse {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("old_stock")
    private Integer oldStock;

    @JsonProperty("new_stock")
    private Integer newStock;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
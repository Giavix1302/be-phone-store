package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OutOfStockResponse DTO for stock issues
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutOfStockResponse {

    @JsonProperty("out_of_stock_items")
    private List<OutOfStockItem> out_of_stock_items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutOfStockItem {
        @JsonProperty("product_name")
        private String product_name;

        @JsonProperty("requested_quantity")
        private Integer requested_quantity;

        @JsonProperty("available_quantity")
        private Integer available_quantity;
    }
}


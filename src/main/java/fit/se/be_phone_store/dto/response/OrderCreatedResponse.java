package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderCreatedResponse DTO for /api/orders creation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedResponse {

    private OrderInfo order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Long id;

        @JsonProperty("order_number")
        private String order_number;

        @JsonProperty("user_id")
        private Long user_id;

        @JsonProperty("total_amount")
        private BigDecimal total_amount;

        private String status;

        @JsonProperty("payment_method")
        private String payment_method;

        @JsonProperty("shipping_address")
        private String shipping_address;

        private String note;

        private List<OrderItemInfo> items;

        @JsonProperty("created_at")
        private LocalDateTime created_at;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long id;

        @JsonProperty("product_id")
        private Long product_id;

        @JsonProperty("product_name")
        private String product_name;

        @JsonProperty("product_image")
        private String product_image;

        @JsonProperty("color_name")
        private String color_name;

        private Integer quantity;

        @JsonProperty("unit_price")
        private BigDecimal unit_price;
    }
}


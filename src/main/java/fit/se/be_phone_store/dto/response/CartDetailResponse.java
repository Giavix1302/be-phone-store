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
 * CartDetailResponse DTO for detailed cart information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailResponse {

    @JsonProperty("cart_id")
    private Long cart_id;

    @JsonProperty("user_id")
    private Long user_id;

    private List<CartItemDetail> items;

    @JsonProperty("total_items")
    private Integer total_items;

    @JsonProperty("total_quantity")
    private Integer total_quantity;

    @JsonProperty("has_unavailable_items")
    private Boolean has_unavailable_items;

    @JsonProperty("created_at")
    private LocalDateTime created_at;

    @JsonProperty("updated_at")
    private LocalDateTime updated_at;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDetail {
        private Long id;
        private ProductInfo product;
        private ColorInfo color;
        private Integer quantity;

        @JsonProperty("unit_price")
        private BigDecimal unit_price;

        @JsonProperty("line_total")
        private BigDecimal line_total;

        @JsonProperty("is_available")
        private Boolean is_available;

        @JsonProperty("stock_status")
        private String stock_status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
        private BigDecimal price;

        @JsonProperty("discount_price")
        private BigDecimal discount_price;

        @JsonProperty("current_price")
        private BigDecimal current_price;

        @JsonProperty("stock_quantity")
        private Integer stock_quantity;

        @JsonProperty("primary_image")
        private String primary_image;

        @JsonProperty("is_active")
        private Boolean is_active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorInfo {
        private Long id;

        @JsonProperty("color_name")
        private String color_name;

        @JsonProperty("hex_code")
        private String hex_code;
    }
}


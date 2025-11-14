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
 * OrderDetailResponseNew DTO for detailed order information (new format)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponseNew {

    private Long id;

    @JsonProperty("order_number")
    private String order_number;

    private UserInfo user;

    @JsonProperty("total_amount")
    private BigDecimal total_amount;
    private String status;

    @JsonProperty("payment_method")
    private String payment_method;

    @JsonProperty("shipping_address")
    private String shipping_address;
    private String note;

    private List<OrderItemDetail> items;

    @JsonProperty("status_history")
    private List<StatusHistory> status_history;

    @JsonProperty("tracking_info")
    private TrackingInfo tracking_info;

    @JsonProperty("created_at")
    private LocalDateTime created_at;

    @JsonProperty("updated_at")
    private LocalDateTime updated_at;

    @JsonProperty("can_cancel")
    private Boolean can_cancel;

    @JsonProperty("can_review")
    private Boolean can_review;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;

        @JsonProperty("full_name")
        private String full_name;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Long id;
        private ProductInfo product;

        @JsonProperty("color_name")
        private String color_name;
        private Integer quantity;

        @JsonProperty("unit_price")
        private BigDecimal unit_price;

        @JsonProperty("line_total")
        private BigDecimal line_total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private String slug;
        private String image;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistory {
        private String status;

        @JsonProperty("changed_at")
        private LocalDateTime changed_at;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingInfo {
        @JsonProperty("estimated_delivery")
        private LocalDateTime estimated_delivery;

        @JsonProperty("shipping_partner")
        private String shipping_partner;

        @JsonProperty("tracking_number")
        private String tracking_number;
    }
}


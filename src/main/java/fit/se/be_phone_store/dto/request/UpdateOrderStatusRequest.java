package fit.se.be_phone_store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UpdateOrderStatusRequest DTO for updating order status (Admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @JsonProperty("status")
    private String status;

    private String note;

    private String location;

    @JsonProperty("tracking_number")
    private String tracking_number;

    @JsonProperty("shipping_partner")
    private String shipping_partner;

    @JsonProperty("estimated_delivery")
    private LocalDateTime estimated_delivery;
}


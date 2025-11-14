package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderTrackingResponse DTO for order tracking information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {

    @JsonProperty("order_number")
    private String order_number;

    @JsonProperty("current_status")
    private String current_status;

    @JsonProperty("tracking_number")
    private String tracking_number;

    @JsonProperty("shipping_partner")
    private String shipping_partner;

    @JsonProperty("estimated_delivery")
    private LocalDateTime estimated_delivery;

    @JsonProperty("tracking_events")
    private List<TrackingEvent> tracking_events;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEvent {
        private String status;
        private String description;
        private String location;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
}


package fit.se.be_phone_store.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CancelOrderRequest DTO for cancelling orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    private String reason;
}


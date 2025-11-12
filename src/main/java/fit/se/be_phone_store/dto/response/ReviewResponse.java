package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ReviewResponse DTO for review data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long userId;
    private String username;
    private String userFullName;
    private Long productId;
    private String productName;
    private Integer rating;
    private String comment;
    private Boolean isPositive;
    private Boolean isNegative;
    private LocalDateTime createdAt;
}

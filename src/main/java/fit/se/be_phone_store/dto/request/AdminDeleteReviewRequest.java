package fit.se.be_phone_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDeleteReviewRequest {

    @NotBlank(message = "Lý do xóa là bắt buộc")
    private String reason;
}


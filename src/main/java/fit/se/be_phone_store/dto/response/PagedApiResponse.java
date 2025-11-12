package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized Paginated API Response wrapper
 * For endpoints that return paginated data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedApiResponse<T> {
    
    private boolean success;
    private String message;
    private List<T> data;
    private PaginationInfo pagination;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    // Success response with paginated data
    public static <T> PagedApiResponse<T> success(String message, Page<T> page) {
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        return PagedApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(page.getContent())
                .pagination(pagination)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Success response with list data and custom pagination
    public static <T> PagedApiResponse<T> success(String message, List<T> data, PaginationInfo pagination) {
        return PagedApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .pagination(pagination)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response for paginated endpoints
    public static <T> PagedApiResponse<T> error(String message) {
        return PagedApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

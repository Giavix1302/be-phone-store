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
 * AdminUserListResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {

    @JsonProperty("users")
    private List<UserInfo> users;

    @JsonProperty("pagination")
    private PaginationInfo pagination;

    @JsonProperty("summary")
    private SummaryInfo summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("email")
        private String email;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("role")
        private String role;

        @JsonProperty("enabled")
        private Boolean enabled;

        @JsonProperty("total_orders")
        private Integer totalOrders;

        @JsonProperty("total_spent")
        private BigDecimal totalSpent;

        @JsonProperty("last_login")
        private LocalDateTime lastLogin;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        @JsonProperty("current_page")
        private Integer currentPage;

        @JsonProperty("total_pages")
        private Integer totalPages;

        @JsonProperty("total_items")
        private Long totalItems;

        @JsonProperty("items_per_page")
        private Integer itemsPerPage;

        @JsonProperty("has_next")
        private Boolean hasNext;

        @JsonProperty("has_prev")
        private Boolean hasPrev;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryInfo {
        @JsonProperty("total_users")
        private Integer totalUsers;

        @JsonProperty("active_users")
        private Integer activeUsers;

        @JsonProperty("disabled_users")
        private Integer disabledUsers;

        @JsonProperty("total_admins")
        private Integer totalAdmins;
    }
}


package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * UserStatisticsAdminResponse - Response DTO for admin user statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsAdminResponse {

    @JsonProperty("period")
    private String period;

    @JsonProperty("overview")
    private OverviewInfo overview;

    @JsonProperty("growth_stats")
    private GrowthStatsInfo growthStats;

    @JsonProperty("daily_registrations")
    private List<DailyRegistration> dailyRegistrations;

    @JsonProperty("top_customers")
    private List<TopCustomer> topCustomers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewInfo {
        @JsonProperty("total_users")
        private Long totalUsers;

        @JsonProperty("active_users")
        private Long activeUsers;

        @JsonProperty("disabled_users")
        private Long disabledUsers;

        @JsonProperty("new_registrations")
        private Long newRegistrations;

        @JsonProperty("users_with_orders")
        private Long usersWithOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthStatsInfo {
        @JsonProperty("new_users_this_period")
        private Long newUsersThisPeriod;

        @JsonProperty("new_users_previous_period")
        private Long newUsersPreviousPeriod;

        @JsonProperty("growth_rate")
        private Double growthRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRegistration {
        @JsonProperty("date")
        private String date;

        @JsonProperty("new_users")
        private Integer newUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("total_spent")
        private BigDecimal totalSpent;

        @JsonProperty("total_orders")
        private Integer totalOrders;
    }
}


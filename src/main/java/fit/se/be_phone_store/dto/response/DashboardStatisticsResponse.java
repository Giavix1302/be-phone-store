package fit.se.be_phone_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DashboardStatisticsResponse DTO for admin dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    // Basic counts
    private Long totalProducts;
    private Long totalUsers;
    private Long totalOrders;
    private Long totalReviews;

    // Revenue statistics
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal averageOrderValue;

    // Order statistics
    private Long pendingOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    // Product statistics
    private Long inStockProducts;
    private Long outOfStockProducts;
    private Long lowStockProducts;

    // User statistics
    private Long activeUsers;
    private Long newUsersThisMonth;

    // Charts data
    private List<Map<String, Object>> monthlyRevenueData;
    private List<Map<String, Object>> orderStatusData;
    private List<Map<String, Object>> topProducts;
    private List<Map<String, Object>> topCategories;

    // Recent activity
    private List<OrderResponse> recentOrders;
    private List<ReviewResponse> recentReviews;
    private List<UserProfileResponse> newUsers;
}

package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.response.DashboardOverviewResponse;
import fit.se.be_phone_store.dto.response.RevenueAnalyticsResponse;
import fit.se.be_phone_store.dto.response.OrdersAnalyticsResponse;
import fit.se.be_phone_store.dto.response.ProductsAnalyticsResponse;
import fit.se.be_phone_store.dto.response.ChartsDataResponse;
import fit.se.be_phone_store.entity.Order;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Review;
import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.exception.UnauthorizedException;
import fit.se.be_phone_store.repository.OrderRepository;
import fit.se.be_phone_store.repository.OrderItemRepository;
import fit.se.be_phone_store.repository.ProductRepository;
import fit.se.be_phone_store.repository.ReviewRepository;
import fit.se.be_phone_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * DashboardService - Handles admin dashboard analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final AuthService authService;

    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Get dashboard overview (Admin only)
     * @param period Period: today, week, month, year
     * @return API response with dashboard overview
     */
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview(String period) {
        log.info("Getting dashboard overview (Admin) - period: {}", period);

        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        LocalDate now = LocalDate.now();
        LocalDate periodStart;
        LocalDate periodEnd = now;

        switch (period.toLowerCase()) {
            case "today":
                periodStart = now;
                periodEnd = now;
                break;
            case "week":
                periodStart = now.minusWeeks(1);
                break;
            case "month":
                periodStart = now.minusMonths(1);
                break;
            case "year":
                periodStart = now.minusYears(1);
                break;
            default:
                periodStart = now;
                period = "today";
        }

        LocalDateTime periodStartDateTime = periodStart.atStartOfDay();
        LocalDateTime periodEndDateTime = periodEnd.atTime(23, 59, 59);

        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        LocalDate previousPeriodStart = periodStart.minusDays(periodDays);
        LocalDate previousPeriodEnd = periodStart.minusDays(1);
        LocalDateTime previousPeriodStartDateTime = previousPeriodStart.atStartOfDay();
        LocalDateTime previousPeriodEndDateTime = previousPeriodEnd.atTime(23, 59, 59);

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.countActiveProducts();

        DashboardOverviewResponse.OverviewInfo overview = DashboardOverviewResponse.OverviewInfo.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .build();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(todayStart, todayEnd);
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long todayOrdersCount = todayOrders.size();
        long todayNewUsers = userRepository.countByCreatedAtBetween(todayStart, todayEnd);
        
        long todayActiveUsers = todayOrders.stream()
                .map(o -> o.getUser().getId())
                .distinct()
                .count();

        DashboardOverviewResponse.TodayStatsInfo todayStats = DashboardOverviewResponse.TodayStatsInfo.builder()
                .revenue(todayRevenue)
                .orders(todayOrdersCount)
                .newUsers(todayNewUsers)
                .activeUsers(todayActiveUsers)
                .build();

        List<Order> periodOrders = orderRepository.findByCreatedAtBetween(periodStartDateTime, periodEndDateTime);
        BigDecimal periodRevenue = periodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Order> previousPeriodOrders = orderRepository.findByCreatedAtBetween(
                previousPeriodStartDateTime, previousPeriodEndDateTime);
        BigDecimal previousPeriodRevenue = previousPeriodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long periodOrdersCount = periodOrders.size();
        long previousPeriodOrdersCount = previousPeriodOrders.size();

        long periodNewUsers = userRepository.countByCreatedAtBetween(periodStartDateTime, periodEndDateTime);
        long previousPeriodNewUsers = userRepository.countByCreatedAtBetween(
                previousPeriodStartDateTime, previousPeriodEndDateTime);

        double revenueGrowth = calculateGrowthRate(previousPeriodRevenue, periodRevenue);
        double ordersGrowth = calculateGrowthRate(
                BigDecimal.valueOf(previousPeriodOrdersCount), 
                BigDecimal.valueOf(periodOrdersCount));
        double usersGrowth = calculateGrowthRate(
                BigDecimal.valueOf(previousPeriodNewUsers), 
                BigDecimal.valueOf(periodNewUsers));

        DashboardOverviewResponse.ComparisonsInfo comparisons = DashboardOverviewResponse.ComparisonsInfo.builder()
                .revenueGrowth(revenueGrowth)
                .ordersGrowth(ordersGrowth)
                .usersGrowth(usersGrowth)
                .build();

        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        
        List<Product> lowStockProducts = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
        long lowStockProductsCount = lowStockProducts.size();

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        List<Review> recentReviews = reviewRepository.findByCreatedAtAfter(last24Hours);
        long recentReviewsCount = recentReviews.size();

        DashboardOverviewResponse.QuickStatsInfo quickStats = DashboardOverviewResponse.QuickStatsInfo.builder()
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProductsCount)
                .recentReviews(recentReviewsCount)
                .activeUsersToday(todayActiveUsers)
                .build();

        return DashboardOverviewResponse.builder()
                .period(period)
                .overview(overview)
                .todayStats(todayStats)
                .comparisons(comparisons)
                .quickStats(quickStats)
                .build();
    }

    /**
     * Calculate growth rate percentage
     */
    private double calculateGrowthRate(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                return 100.0; 
            }
            return 0.0;
        }
        
        if (current == null) {
            current = BigDecimal.ZERO;
        }
        
        double growth = ((current.subtract(previous)).divide(previous, 4, java.math.RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        
        return Math.round(growth * 10.0) / 10.0; 
    }

    /**
     * Get revenue analytics (Admin only)
     * @param period Period: today, week, month, year, custom
     * @param fromDate Start date for custom period
     * @param toDate End date for custom period
     * @return Revenue analytics response
     */
    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse getRevenueAnalytics(String period, LocalDate fromDate, LocalDate toDate) {
        log.info("Getting revenue analytics (Admin) - period: {}", period);

        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        LocalDate now = LocalDate.now();
        LocalDate periodStart;
        LocalDate periodEnd = now;

        if (fromDate != null && toDate != null) {
            periodStart = fromDate;
            periodEnd = toDate;
            period = "custom";
        } else {
            switch (period.toLowerCase()) {
                case "today":
                    periodStart = now;
                    periodEnd = now;
                    break;
                case "week":
                    periodStart = now.minusWeeks(1);
                    break;
                case "month":
                    periodStart = now.minusMonths(1);
                    break;
                case "year":
                    periodStart = now.minusYears(1);
                    break;
                default:
                    periodStart = now.minusMonths(1);
                    period = "month";
            }
        }

        LocalDateTime periodStartDateTime = periodStart.atStartOfDay();
        LocalDateTime periodEndDateTime = periodEnd.atTime(23, 59, 59);

        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        LocalDate previousPeriodStart = periodStart.minusDays(periodDays);
        LocalDate previousPeriodEnd = periodStart.minusDays(1);
        LocalDateTime previousPeriodStartDateTime = previousPeriodStart.atStartOfDay();
        LocalDateTime previousPeriodEndDateTime = previousPeriodEnd.atTime(23, 59, 59);

        List<Order> periodOrders = orderRepository.findByCreatedAtBetween(periodStartDateTime, periodEndDateTime);
        List<Order> deliveredOrders = periodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .collect(java.util.stream.Collectors.toList());

        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = deliveredOrders.size();

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Order> previousPeriodOrders = orderRepository.findByCreatedAtBetween(
                previousPeriodStartDateTime, previousPeriodEndDateTime);
        BigDecimal previousPeriodRevenue = previousPeriodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double revenueGrowth = calculateGrowthRate(previousPeriodRevenue, totalRevenue);

        RevenueAnalyticsResponse.RevenueSummaryInfo revenueSummary = RevenueAnalyticsResponse.RevenueSummaryInfo.builder()
                .totalRevenue(totalRevenue)
                .revenueGrowth(revenueGrowth)
                .averageOrderValue(averageOrderValue)
                .totalOrders(totalOrders)
                .build();

        List<Object[]> dailyStatsData = orderRepository.getDailyOrderStatisticsByDateRange(
                periodStartDateTime, periodEndDateTime);
        
        List<RevenueAnalyticsResponse.DailyRevenue> dailyRevenue = dailyStatsData.stream()
                .map(data -> {
                    LocalDate date;
                    if (data[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) data[0]).toLocalDate();
                    } else if (data[0] instanceof java.time.LocalDate) {
                        date = (LocalDate) data[0];
                    } else if (data[0] instanceof java.util.Date) {
                        date = ((java.util.Date) data[0]).toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                    } else {
                        date = LocalDate.parse(data[0].toString());
                    }
                    
                    Long ordersCount = ((Number) data[1]).longValue();
                    BigDecimal revenue;
                    if (data[2] == null) {
                        revenue = BigDecimal.ZERO;
                    } else if (data[2] instanceof BigDecimal) {
                        revenue = (BigDecimal) data[2];
                    } else {
                        Number numValue = (Number) data[2];
                        revenue = new BigDecimal(numValue.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                    
                    return RevenueAnalyticsResponse.DailyRevenue.builder()
                            .date(date.toString())
                            .revenue(revenue)
                            .orders(ordersCount.intValue())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        List<Object[]> revenueByCategoryData = orderItemRepository.findRevenueByCategoryInDateRange(
                periodStartDateTime, periodEndDateTime);
        
        BigDecimal totalCategoryRevenue = revenueByCategoryData.stream()
                .map(data -> {
                    if (data[1] == null) {
                        return BigDecimal.ZERO;
                    } else if (data[1] instanceof BigDecimal) {
                        return (BigDecimal) data[1];
                    } else {
                        Number numValue = (Number) data[1];
                        return new BigDecimal(numValue.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RevenueAnalyticsResponse.RevenueByCategory> revenueByCategory = revenueByCategoryData.stream()
                .map(data -> {
                    String categoryName = (String) data[0];
                    BigDecimal categoryRevenue;
                    if (data[1] == null) {
                        categoryRevenue = BigDecimal.ZERO;
                    } else if (data[1] instanceof BigDecimal) {
                        categoryRevenue = (BigDecimal) data[1];
                    } else {
                        Number numValue = (Number) data[1];
                        categoryRevenue = new BigDecimal(numValue.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                    
                    double percentage = 0.0;
                    if (totalCategoryRevenue.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = categoryRevenue.divide(totalCategoryRevenue, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                        percentage = Math.round(percentage * 10.0) / 10.0;
                    }
                    
                    return RevenueAnalyticsResponse.RevenueByCategory.builder()
                            .category(categoryName)
                            .revenue(categoryRevenue)
                            .percentage(percentage)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        List<Object[]> topProductsData = orderItemRepository.findTopProductsByRevenueInDateRange(
                periodStartDateTime, periodEndDateTime, 10);
        
        List<RevenueAnalyticsResponse.TopRevenueProduct> topRevenueProducts = topProductsData.stream()
                .map(data -> {
                    Long productId = ((Number) data[0]).longValue();
                    String productName = (String) data[1];
                    Long quantitySold = ((Number) data[2]).longValue();
                    BigDecimal revenue;
                    if (data[3] == null) {
                        revenue = BigDecimal.ZERO;
                    } else if (data[3] instanceof BigDecimal) {
                        revenue = (BigDecimal) data[3];
                    } else {
                        Number numValue = (Number) data[3];
                        revenue = new BigDecimal(numValue.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                    
                    return RevenueAnalyticsResponse.TopRevenueProduct.builder()
                            .productId(productId)
                            .productName(productName)
                            .revenue(revenue)
                            .quantitySold(quantitySold.intValue())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        return RevenueAnalyticsResponse.builder()
                .period(period)
                .revenueSummary(revenueSummary)
                .dailyRevenue(dailyRevenue)
                .revenueByCategory(revenueByCategory)
                .topRevenueProducts(topRevenueProducts)
                .build();
    }

    /**
     * Get orders analytics (Admin only)
     * @param period Period: today, week, month, year, custom
     * @param fromDate Start date for custom period
     * @param toDate End date for custom period
     * @return Orders analytics response
     */
    @Transactional(readOnly = true)
    public OrdersAnalyticsResponse getOrdersAnalytics(String period, LocalDate fromDate, LocalDate toDate) {
        log.info("Getting orders analytics (Admin) - period: {}", period);

        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        LocalDate now = LocalDate.now();
        LocalDate periodStart;
        LocalDate periodEnd = now;

        if (fromDate != null && toDate != null) {
            periodStart = fromDate;
            periodEnd = toDate;
            period = "custom";
        } else {
            switch (period.toLowerCase()) {
                case "today":
                    periodStart = now;
                    periodEnd = now;
                    break;
                case "week":
                    periodStart = now.minusWeeks(1);
                    break;
                case "month":
                    periodStart = now.minusMonths(1);
                    break;
                case "year":
                    periodStart = now.minusYears(1);
                    break;
                default:
                    periodStart = now.minusMonths(1);
                    period = "month";
            }
        }

        LocalDateTime periodStartDateTime = periodStart.atStartOfDay();
        LocalDateTime periodEndDateTime = periodEnd.atTime(23, 59, 59);

        List<Order> periodOrders = orderRepository.findByCreatedAtBetween(periodStartDateTime, periodEndDateTime);

        long totalOrders = periodOrders.size();
        long completedOrders = periodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .count();
        long cancelledOrders = periodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
                .count();
        
        double completionRate = totalOrders > 0
                ? (double) completedOrders / totalOrders * 100.0
                : 0.0;
        completionRate = Math.round(completionRate * 10.0) / 10.0;

        BigDecimal totalRevenue = periodOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = completedOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        OrdersAnalyticsResponse.OrdersSummaryInfo ordersSummary = OrdersAnalyticsResponse.OrdersSummaryInfo.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .completionRate(completionRate)
                .averageOrderValue(averageOrderValue)
                .build();

        List<Object[]> dailyOrdersData = orderRepository.getDailyOrdersWithStatus(
                periodStartDateTime, periodEndDateTime);
        
        List<OrdersAnalyticsResponse.DailyOrder> dailyOrders = dailyOrdersData.stream()
                .map(data -> {
                    LocalDate date;
                    if (data[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) data[0]).toLocalDate();
                    } else if (data[0] instanceof java.time.LocalDate) {
                        date = (LocalDate) data[0];
                    } else if (data[0] instanceof java.util.Date) {
                        date = ((java.util.Date) data[0]).toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                    } else {
                        date = LocalDate.parse(data[0].toString());
                    }
                    
                    Long total = ((Number) data[1]).longValue();
                    Long completed = ((Number) data[2]).longValue();
                    Long cancelled = ((Number) data[3]).longValue();
                    
                    return OrdersAnalyticsResponse.DailyOrder.builder()
                            .date(date.toString())
                            .orders(total.intValue())
                            .completed(completed.intValue())
                            .cancelled(cancelled.intValue())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        List<Object[]> ordersByStatusData = orderRepository.countOrdersByStatusInDateRange(
                periodStartDateTime, periodEndDateTime);
        
        Map<String, Integer> ordersByStatus = new java.util.HashMap<>();
        for (Object[] data : ordersByStatusData) {
            String status;
            if (data[0] instanceof Order.OrderStatus) {
                status = ((Order.OrderStatus) data[0]).name();
            } else {
                status = data[0].toString();
            }
            Integer count = ((Number) data[1]).intValue();
            ordersByStatus.put(status, count);
        }

        List<Object[]> peakHoursData = orderRepository.getOrdersByHour(
                periodStartDateTime, periodEndDateTime);
        
        List<OrdersAnalyticsResponse.PeakHour> peakHours = peakHoursData.stream()
                .limit(10)
                .map(data -> {
                    Integer hour = ((Number) data[0]).intValue();
                    Integer orders = ((Number) data[1]).intValue();
                    
                    return OrdersAnalyticsResponse.PeakHour.builder()
                            .hour(hour)
                            .orders(orders)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        return OrdersAnalyticsResponse.builder()
                .period(period)
                .ordersSummary(ordersSummary)
                .dailyOrders(dailyOrders)
                .ordersByStatus(ordersByStatus)
                .peakHours(peakHours)
                .build();
    }

    /**
     * Get products analytics (Admin only)
     * @return Products analytics response
     */
    @Transactional(readOnly = true)
    public ProductsAnalyticsResponse getProductsAnalytics() {
        log.info("Getting products analytics (Admin)");

        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countActiveProducts();
        long inactiveProducts = totalProducts - activeProducts;
        
        List<Product> lowStockProducts = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
        long lowStockCount = lowStockProducts.size();
        
        List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();
        long outOfStockCount = outOfStockProducts.size();

        ProductsAnalyticsResponse.ProductsSummaryInfo productsSummary = 
                ProductsAnalyticsResponse.ProductsSummaryInfo.builder()
                        .totalProducts(totalProducts)
                        .activeProducts(activeProducts)
                        .inactiveProducts(inactiveProducts)
                        .lowStockProducts(lowStockCount)
                        .outOfStockProducts(outOfStockCount)
                        .build();

        List<Object[]> bestSellingData = orderItemRepository.findBestSellingProductsWithDetails();
        
        List<ProductsAnalyticsResponse.BestSellingProduct> bestSellingProducts = 
                bestSellingData.stream()
                        .limit(10)
                        .map(data -> {
                            Long productId = ((Number) data[0]).longValue();
                            String productName = (String) data[1];
                            Long quantitySold = ((Number) data[2]).longValue();
                            Number revenueNum = (Number) data[3];
                            Integer stockRemaining = ((Number) data[4]).intValue();
                            
                            BigDecimal revenue = new BigDecimal(revenueNum.toString())
                                    .setScale(2, java.math.RoundingMode.HALF_UP);
                            
                            return ProductsAnalyticsResponse.BestSellingProduct.builder()
                                    .productId(productId)
                                    .productName(productName)
                                    .quantitySold(quantitySold.intValue())
                                    .revenue(revenue)
                                    .stockRemaining(stockRemaining)
                                    .build();
                        })
                        .collect(java.util.stream.Collectors.toList());

        List<ProductsAnalyticsResponse.LowStockAlert> lowStockAlerts = 
                lowStockProducts.stream()
                        .map(product -> ProductsAnalyticsResponse.LowStockAlert.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .currentStock(product.getStockQuantity())
                                .recommendedReorder(20)
                                .build())
                        .collect(java.util.stream.Collectors.toList());

        List<Object[]> productsByCategoryData = productRepository.countProductsByCategory();
        Map<String, Long> totalProductsByCategory = new java.util.HashMap<>();
        for (Object[] data : productsByCategoryData) {
            String categoryName = (String) data[0];
            Long count = ((Number) data[1]).longValue();
            totalProductsByCategory.put(categoryName, count);
        }
        
        List<Object[]> categoryPerformanceData = orderItemRepository.findCategoryPerformance();
        
        List<ProductsAnalyticsResponse.CategoryPerformance> categoryPerformance = 
                categoryPerformanceData.stream()
                        .map(data -> {
                            String category = (String) data[0];
                            Long productsSold = ((Number) data[1]).longValue();
                            Number revenueNum = (Number) data[2];

                            Long totalProductsInCategory = totalProductsByCategory.getOrDefault(category, 0L);
                            
                            BigDecimal revenue = new BigDecimal(revenueNum.toString())
                                    .setScale(2, java.math.RoundingMode.HALF_UP);
                            
                            return ProductsAnalyticsResponse.CategoryPerformance.builder()
                                    .category(category)
                                    .totalProducts(totalProductsInCategory)
                                    .productsSold(productsSold)
                                    .revenue(revenue)
                                    .build();
                        })
                        .collect(java.util.stream.Collectors.toList());

        return ProductsAnalyticsResponse.builder()
                .productsSummary(productsSummary)
                .bestSellingProducts(bestSellingProducts)
                .lowStockAlerts(lowStockAlerts)
                .categoryPerformance(categoryPerformance)
                .build();
    }

    /**
     * Get charts data (Admin only)
     * @param chartType Chart type: revenue, orders, users
     * @param period Period: week, month, year, custom
     * @param fromDate Start date for custom period
     * @param toDate End date for custom period
     * @return Charts data response
     */
    @Transactional(readOnly = true)
    public ChartsDataResponse getChartsData(String chartType, String period, LocalDate fromDate, LocalDate toDate) {
        log.info("Getting charts data (Admin) - chart_type: {}, period: {}", chartType, period);

        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        LocalDate now = LocalDate.now();
        LocalDate periodStart;
        LocalDate periodEnd = now;

        if (fromDate != null && toDate != null) {
            periodStart = fromDate;
            periodEnd = toDate;
            period = "custom";
        } else {
            switch (period.toLowerCase()) {
                case "week":
                    periodStart = now.minusWeeks(1);
                    break;
                case "month":
                    periodStart = now.minusMonths(1);
                    break;
                case "year":
                    periodStart = now.minusYears(1);
                    break;
                default:
                    periodStart = now.minusMonths(1);
                    period = "month";
            }
        }

        LocalDateTime periodStartDateTime = periodStart.atStartOfDay();
        LocalDateTime periodEndDateTime = periodEnd.atTime(23, 59, 59);

        switch (chartType.toLowerCase()) {
            case "revenue":
                return getRevenueChartData(period, periodStartDateTime, periodEndDateTime);
            case "orders":
                return getOrdersChartData(period, periodStartDateTime, periodEndDateTime);
            case "users":
                return getUsersChartData(period, periodStartDateTime, periodEndDateTime);
            default:
                throw new fit.se.be_phone_store.exception.BadRequestException(
                        "Invalid chart_type. Must be: revenue, orders, or users");
        }
    }

    private ChartsDataResponse getRevenueChartData(String period, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> dailyRevenueData = orderRepository.getDailyRevenueForCharts(startDate, endDate);

        List<String> labels = new java.util.ArrayList<>();
        List<BigDecimal> revenueData = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal peakValue = BigDecimal.ZERO;
        String peakDay = null;

        for (Object[] data : dailyRevenueData) {
            LocalDate date;
            if (data[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) data[0]).toLocalDate();
            } else if (data[0] instanceof java.time.LocalDate) {
                date = (LocalDate) data[0];
            } else if (data[0] instanceof java.util.Date) {
                date = ((java.util.Date) data[0]).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                date = LocalDate.parse(data[0].toString());
            }

            Number revenueNum = (Number) data[1];
            BigDecimal revenue = new BigDecimal(revenueNum.toString())
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            labels.add(date.toString());
            revenueData.add(revenue);
            total = total.add(revenue);

            if (revenue.compareTo(peakValue) > 0) {
                peakValue = revenue;
                peakDay = date.toString();
            }
        }

        BigDecimal average = revenueData.isEmpty() ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(revenueData.size()), 2, java.math.RoundingMode.HALF_UP);

        ChartsDataResponse.Dataset dataset = ChartsDataResponse.Dataset.builder()
                .label("Doanh thu")
                .data(new java.util.ArrayList<>(revenueData))
                .backgroundColor("#3B82F6")
                .borderColor("#1D4ED8")
                .build();

        ChartsDataResponse.SummaryInfo summary = ChartsDataResponse.SummaryInfo.builder()
                .total(total)
                .average(average)
                .peakDay(peakDay)
                .peakValue(peakValue)
                .build();

        return ChartsDataResponse.builder()
                .chartType("revenue")
                .period(period)
                .labels(labels)
                .datasets(java.util.Arrays.asList(dataset))
                .summary(summary)
                .build();
    }

    private ChartsDataResponse getOrdersChartData(String period, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> dailyOrdersData = orderRepository.getDailyOrdersForCharts(startDate, endDate);

        List<String> labels = new java.util.ArrayList<>();
        List<Long> ordersData = new java.util.ArrayList<>();
        long total = 0;
        long peakValue = 0;
        String peakDay = null;

        for (Object[] data : dailyOrdersData) {
            LocalDate date;
            if (data[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) data[0]).toLocalDate();
            } else if (data[0] instanceof java.time.LocalDate) {
                date = (LocalDate) data[0];
            } else if (data[0] instanceof java.util.Date) {
                date = ((java.util.Date) data[0]).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                date = LocalDate.parse(data[0].toString());
            }

            Long orders = ((Number) data[1]).longValue();

            labels.add(date.toString());
            ordersData.add(orders);
            total += orders;

            if (orders > peakValue) {
                peakValue = orders;
                peakDay = date.toString();
            }
        }

        BigDecimal average = ordersData.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(total).divide(BigDecimal.valueOf(ordersData.size()), 2, java.math.RoundingMode.HALF_UP);

        ChartsDataResponse.Dataset dataset = ChartsDataResponse.Dataset.builder()
                .label("Đơn hàng")
                .data(new java.util.ArrayList<>(ordersData))
                .backgroundColor("#8B5CF6")
                .borderColor("#6D28D9")
                .build();

        ChartsDataResponse.SummaryInfo summary = ChartsDataResponse.SummaryInfo.builder()
                .total(BigDecimal.valueOf(total))
                .average(average)
                .peakDay(peakDay)
                .peakValue(BigDecimal.valueOf(peakValue))
                .build();

        return ChartsDataResponse.builder()
                .chartType("orders")
                .period(period)
                .labels(labels)
                .datasets(java.util.Arrays.asList(dataset))
                .summary(summary)
                .build();
    }

    private ChartsDataResponse getUsersChartData(String period, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> weeklyData = userRepository.getWeeklyUserGrowth(startDate, endDate);

        List<String> labels = new java.util.ArrayList<>();
        List<Long> newUsersData = new java.util.ArrayList<>();
        List<Long> totalUsersData = new java.util.ArrayList<>();

        for (Object[] data : weeklyData) {
            Integer year = ((Number) data[0]).intValue();
            Integer week = ((Number) data[1]).intValue();
            Long newUsers = ((Number) data[2]).longValue();
            Long totalUsers = ((Number) data[3]).longValue();

            labels.add("Week " + week);
            newUsersData.add(newUsers);
            totalUsersData.add(totalUsers);
        }

        ChartsDataResponse.Dataset newUsersDataset = ChartsDataResponse.Dataset.builder()
                .label("Người dùng mới")
                .data(new java.util.ArrayList<>(newUsersData))
                .backgroundColor("#10B981")
                .borderColor("#059669")
                .build();

        ChartsDataResponse.Dataset totalUsersDataset = ChartsDataResponse.Dataset.builder()
                .label("Tổng người dùng")
                .data(new java.util.ArrayList<>(totalUsersData))
                .backgroundColor("#6366F1")
                .borderColor("#4338CA")
                .build();

        return ChartsDataResponse.builder()
                .chartType("users")
                .period(period)
                .labels(labels)
                .datasets(java.util.Arrays.asList(newUsersDataset, totalUsersDataset))
                .build();
    }
}


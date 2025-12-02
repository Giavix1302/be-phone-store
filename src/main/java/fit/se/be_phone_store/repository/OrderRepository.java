package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Order;
import fit.se.be_phone_store.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderRepository interface for Order entity operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders by user
    List<Order> findByUser(User user);
    
    List<Order> findByUserId(Long userId);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    // Find orders by status
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by user and status
    List<Order> findByUserAndStatus(User user, Order.OrderStatus status);
    
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
    
    // Find orders by date range
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find orders created after specific date
    List<Order> findByCreatedAtAfter(LocalDateTime date);
    
    // Find orders created before specific date
    List<Order> findByCreatedAtBefore(LocalDateTime date);
    
    // Find orders by total amount range
    List<Order> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find recent orders
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);
    
    // Find orders by status ordered by date
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
    
    // Count orders by status
    long countByStatus(Order.OrderStatus status);
    
    // Count orders by user
    long countByUser(User user);
    
    long countByUserId(Long userId);
    
    // Calculate total revenue
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();
    
    // Calculate revenue by date range
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Calculate revenue by month
    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), SUM(o.totalAmount) " +
           "FROM Order o WHERE o.status = 'DELIVERED' " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> calculateMonthlyRevenue();
    
    // Find top customers by order count
    @Query("SELECT o.user, COUNT(o) as orderCount FROM Order o GROUP BY o.user ORDER BY orderCount DESC")
    List<Object[]> findTopCustomersByOrderCount();
    
    // Find top customers by total amount spent
    @Query("SELECT o.user, SUM(o.totalAmount) as totalSpent FROM Order o WHERE o.status = 'DELIVERED' GROUP BY o.user ORDER BY totalSpent DESC")
    List<Object[]> findTopCustomersByTotalSpent();
    
    // Average order value
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal calculateAverageOrderValue();
    
    // Find orders with shipping address containing keyword
    @Query("SELECT o FROM Order o WHERE LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Order> findByShippingAddressContaining(@Param("keyword") String keyword);
    
    // Find pending orders older than specific date (for follow-up)
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt < :date")
    List<Order> findPendingOrdersOlderThan(@Param("date") LocalDateTime date);
    
    // Find orders that can be cancelled
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED')")
    List<Order> findCancellableOrders();
    
    // Daily order statistics
    @Query("SELECT DATE(o.createdAt), COUNT(o), SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.createdAt >= :startDate " +
           "GROUP BY DATE(o.createdAt) " +
           "ORDER BY DATE(o.createdAt) DESC")
    List<Object[]> getDailyOrderStatistics(@Param("startDate") LocalDateTime startDate);
    
    // Daily order statistics with date range
    @Query("SELECT FUNCTION('DATE', o.createdAt), COUNT(o), SUM(CASE WHEN o.status = 'DELIVERED' THEN o.totalAmount ELSE 0 END) " +
           "FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) " +
           "ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<Object[]> getDailyOrderStatisticsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Orders by status count
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
    
    // Check if user has any orders
    boolean existsByUser(User user);
    
    boolean existsByUserId(Long userId);
    
    // Find orders needing shipping update
    @Query("SELECT o FROM Order o WHERE o.status = 'PROCESSING' AND o.createdAt < :date")
    List<Order> findOrdersNeedingShippingUpdate(@Param("date") LocalDateTime date);
    
    // Find orders with filters (Admin)
    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status = :status)
          AND (:userId IS NULL OR o.user.id = :userId)
          AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
          AND (:toDate IS NULL OR o.createdAt <= :toDate)
          AND (:search IS NULL OR 
               LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(o.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(o.user.phone) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Order> findOrdersWithFilters(
        @Param("status") Order.OrderStatus status,
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("search") String search,
        Pageable pageable
    );
}

package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.OrderItem;
import fit.se.be_phone_store.entity.Order;
import fit.se.be_phone_store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderItemRepository interface for OrderItem entity operations
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find all items in an order
    List<OrderItem> findByOrder(Order order);
    
    List<OrderItem> findByOrderId(Long orderId);
    
    // Find items by product
    List<OrderItem> findByProduct(Product product);
    
    List<OrderItem> findByProductId(Long productId);
    
    // Count items in order
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);
    
    // Calculate total quantity in order
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Integer sumQuantityByOrderId(@Param("orderId") Long orderId);
    
    // Calculate total price for order items
    @Query("SELECT COALESCE(SUM(oi.quantity * oi.unitPrice), 0) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Double calculateTotalByOrderId(@Param("orderId") Long orderId);
    
    // Find best selling products
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY oi.product " +
           "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();
    
    // Find top revenue generating products
    @Query("SELECT oi.product, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY oi.product " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> findTopRevenueProducts();
    
    // Find products sold in date range
    @Query("SELECT oi.product, SUM(oi.quantity) as quantitySold " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product " +
           "ORDER BY quantitySold DESC")
    List<Object[]> findProductsSoldInDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    // Find top products by revenue in date range
    @Query(value = "SELECT oi.product_id, p.name, SUM(oi.quantity) as quantitySold, SUM(oi.quantity * oi.unit_price) as revenue " +
           "FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.id " +
           "JOIN products p ON oi.product_id = p.id " +
           "WHERE o.status = 'DELIVERED' AND o.created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product_id, p.name " +
           "ORDER BY revenue DESC " +
           "LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findTopProductsByRevenueInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") int limit
    );
    
    // Calculate total quantity sold for a product
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.product.id = :productId AND o.status = 'DELIVERED'")
    Integer calculateTotalSoldByProductId(@Param("productId") Long productId);
    
    // Calculate total revenue for a product
    @Query("SELECT COALESCE(SUM(oi.quantity * oi.unitPrice), 0) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.product.id = :productId AND o.status = 'DELIVERED'")
    Double calculateRevenueByProductId(@Param("productId") Long productId);
    
    // Find order items by user
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);
    
    // Find recently sold items
    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND o.createdAt >= :since " +
           "ORDER BY o.createdAt DESC")
    List<OrderItem> findRecentlySoldItems(@Param("since") LocalDateTime since);
    
    // Find items with specific unit price
    List<OrderItem> findByUnitPrice(Double unitPrice);
    
    // Find items with quantity greater than specified
    List<OrderItem> findByQuantityGreaterThan(Integer quantity);
    
    // Monthly sales statistics
    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), SUM(oi.quantity), SUM(oi.quantity * oi.unitPrice) " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> getMonthlySalesStatistics();
    
    // Average item price
    @Query("SELECT AVG(oi.unitPrice) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED'")
    Double calculateAverageItemPrice();
    
    // Find products frequently bought together
    @Query("SELECT oi1.product, oi2.product, COUNT(*) as frequency " +
           "FROM OrderItem oi1 " +
           "JOIN OrderItem oi2 ON oi1.order = oi2.order " +
           "WHERE oi1.product.id < oi2.product.id " +
           "GROUP BY oi1.product, oi2.product " +
           "HAVING COUNT(*) > 1 " +
           "ORDER BY frequency DESC")
    List<Object[]> findFrequentlyBoughtTogether();
    
    // Delete items by order
    void deleteByOrder(Order order);
    
    void deleteByOrderId(Long orderId);
    
    // Check if user has purchased product with DELIVERED status
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.product.id = :productId AND o.user.id = :userId AND o.status = 'DELIVERED'")
    boolean hasUserPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("SELECT MIN(COALESCE(o.updatedAt, o.createdAt)) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.product.id = :productId AND o.user.id = :userId AND o.status = 'DELIVERED'")
    LocalDateTime findFirstPurchaseDate(@Param("userId") Long userId, @Param("productId") Long productId);
}

package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Order;
import fit.se.be_phone_store.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderTrackingRepository interface for OrderTracking entity operations
 */
@Repository
public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {

    // Find all tracking events for an order
    List<OrderTracking> findByOrder(Order order);
    
    List<OrderTracking> findByOrderId(Long orderId);
    
    // Find tracking events by order number
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.order.orderNumber = :orderNumber ORDER BY ot.createdAt ASC")
    List<OrderTracking> findByOrderNumber(@Param("orderNumber") String orderNumber);
    
    // Find latest tracking event for an order
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.order.id = :orderId ORDER BY ot.createdAt DESC")
    List<OrderTracking> findLatestByOrderId(@Param("orderId") Long orderId);
    
    // Find tracking events by status
    List<OrderTracking> findByStatus(Order.OrderStatus status);
    
    // Count tracking events for an order
    long countByOrder(Order order);
    
    long countByOrderId(Long orderId);
}


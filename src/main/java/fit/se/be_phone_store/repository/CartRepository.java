package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Cart;
import fit.se.be_phone_store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CartRepository interface for Cart entity operations
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart by user
    Optional<Cart> findByUser(User user);
    
    Optional<Cart> findByUserId(Long userId);
    
    // Find all carts for user (in case multiple carts exist)
    List<Cart> findAllByUser(User user);
    
    List<Cart> findAllByUserId(Long userId);
    
    // Find carts created after specific date
    List<Cart> findByCreatedAtAfter(LocalDateTime date);
    
    // Find carts created before specific date (for cleanup)
    List<Cart> findByCreatedAtBefore(LocalDateTime date);
    
    // Find carts with items
    @Query("SELECT c FROM Cart c WHERE c.id IN (SELECT DISTINCT ci.cart.id FROM CartItem ci)")
    List<Cart> findCartsWithItems();
    
    // Find empty carts
    @Query("SELECT c FROM Cart c WHERE c.id NOT IN (SELECT DISTINCT ci.cart.id FROM CartItem ci)")
    List<Cart> findEmptyCarts();
    
    // Count total items in user's cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.cartItems ci WHERE c.user.id = :userId")
    Integer countItemsInUserCart(@Param("userId") Long userId);
    
    // Calculate total amount in user's cart
    @Query("SELECT COALESCE(SUM(ci.quantity * ci.unitPrice), 0) FROM Cart c JOIN c.cartItems ci WHERE c.user.id = :userId")
    Double calculateCartTotal(@Param("userId") Long userId);
    
    // Delete empty carts older than specified date
    @Query("DELETE FROM Cart c WHERE c.createdAt < :date AND c.id NOT IN (SELECT DISTINCT ci.cart.id FROM CartItem ci)")
    void deleteEmptyCartsOlderThan(@Param("date") LocalDateTime date);
    
    // Check if user has cart
    boolean existsByUser(User user);
    
    boolean existsByUserId(Long userId);
    
    // Find recent carts (last 30 days)
    @Query("SELECT c FROM Cart c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Cart> findRecentCarts(@Param("since") LocalDateTime since);
}

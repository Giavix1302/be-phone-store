package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.CartItem;
import fit.se.be_phone_store.entity.Cart;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CartItemRepository interface for CartItem entity operations
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find all items in a cart
    List<CartItem> findByCart(Cart cart);
    
    List<CartItem> findByCartId(Long cartId);
    
    // Find specific item in cart
    Optional<CartItem> findByCartAndProductAndColor(Cart cart, Product product, Color color);
    
    Optional<CartItem> findByCartIdAndProductIdAndColorId(Long cartId, Long productId, Long colorId);
    
    // Find items by user
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);
    
    // Find items by product
    List<CartItem> findByProduct(Product product);
    
    List<CartItem> findByProductId(Long productId);
    
    // Check if item exists in cart
    boolean existsByCartAndProductAndColor(Cart cart, Product product, Color color);
    
    boolean existsByCartIdAndProductIdAndColorId(Long cartId, Long productId, Long colorId);
    
    // Delete specific item
    void deleteByCartAndProductAndColor(Cart cart, Product product, Color color);
    
    void deleteByCartIdAndProductIdAndColorId(Long cartId, Long productId, Long colorId);
    
    // Delete all items in cart
    void deleteByCart(Cart cart);
    
    void deleteByCartId(Long cartId);
    
    // Count items in cart
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countByCartId(@Param("cartId") Long cartId);
    
    // Calculate total quantity in cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") Long cartId);
    
    // Calculate total price for cart
    @Query("SELECT COALESCE(SUM(ci.quantity * ci.unitPrice), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Double calculateTotalByCartId(@Param("cartId") Long cartId);
    
    // Find items by user with product details
    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.product p " +
           "JOIN FETCH ci.color c " +
           "WHERE ci.cart.user.id = :userId")
    List<CartItem> findByUserIdWithDetails(@Param("userId") Long userId);
    
    // Find most popular products in carts
    @Query("SELECT ci.product, SUM(ci.quantity) as totalQuantity " +
           "FROM CartItem ci " +
           "GROUP BY ci.product " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findMostPopularProductsInCarts();
    
    // Find items with specific color
    List<CartItem> findByColor(Color color);
    
    List<CartItem> findByColorId(Long colorId);
    
    // Find items where price might have changed
    @Query("SELECT ci FROM CartItem ci " +
           "JOIN ci.product p " +
           "WHERE ci.unitPrice != COALESCE(p.discountPrice, p.price)")
    List<CartItem> findItemsWithPriceChanges();
    
    // Update item quantity
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.id = :itemId")
    void updateQuantity(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);
    
    // Update item price (for price sync)
    @Query("UPDATE CartItem ci SET ci.unitPrice = :newPrice WHERE ci.id = :itemId")
    void updatePrice(@Param("itemId") Long itemId, @Param("newPrice") Double newPrice);
}

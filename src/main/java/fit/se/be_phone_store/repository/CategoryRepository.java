package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository interface for Category entity operations
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Basic queries
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // Find all with sorting
    List<Category> findAll(Sort sort);

    // Find categories with products
    @Query("SELECT DISTINCT c FROM Category c WHERE EXISTS (SELECT 1 FROM Product p WHERE p.category = c)")
    List<Category> findCategoriesWithProducts();

    // Count products in each category
    @Query("SELECT c, SIZE(c.products) FROM Category c")
    List<Object[]> countProductsByCategory();

    // Count active products in each category
    @Query("SELECT c.id, c.name, COUNT(p) as productCount " +
            "FROM Category c LEFT JOIN c.products p " +
            "WHERE p.isActive = true OR p.id IS NULL " +
            "GROUP BY c.id, c.name")
    List<Object[]> countActiveProductsByCategory();

    // Get category statistics
    @Query("SELECT c.id, c.name, " +
            "COUNT(p) as totalProducts, " +
            "SUM(CASE WHEN p.isActive = true THEN 1 ELSE 0 END) as activeProducts, " +
            "SUM(CASE WHEN p.isActive = false THEN 1 ELSE 0 END) as inactiveProducts, " +
            "SUM(CASE WHEN p.stockQuantity > 0 THEN 1 ELSE 0 END) as productsInStock " +
            "FROM Category c LEFT JOIN c.products p " +
            "WHERE c.id = :categoryId " +
            "GROUP BY c.id, c.name")
    List<Object[]> getCategoryStatistics(@Param("categoryId") Long categoryId);

    // Revenue by category
    @Query("SELECT c.id, c.name, COALESCE(SUM(oi.unitPrice * oi.quantity), 0) as revenue " +
            "FROM Category c " +
            "LEFT JOIN c.products p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "LEFT JOIN oi.order o ON o.status = 'DELIVERED' " +
            "GROUP BY c.id, c.name " +
            "ORDER BY revenue DESC")
    List<Object[]> getCategoryRevenue();

    // Brand breakdown by category
    @Query("SELECT p.brand.id, p.brand.name, COUNT(p) as productCount, " +
            "COALESCE(SUM(oi.unitPrice * oi.quantity), 0) as revenue " +
            "FROM Category c " +
            "LEFT JOIN c.products p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "LEFT JOIN oi.order o ON o.status = 'DELIVERED' " +
            "WHERE c.id = :categoryId AND p.brand IS NOT NULL " +
            "GROUP BY p.brand.id, p.brand.name " +
            "ORDER BY revenue DESC")
    List<Object[]> getBrandBreakdownByCategory(@Param("categoryId") Long categoryId);

    // Price range by category
    @Query("SELECT MIN(p.price), MAX(p.price), AVG(p.price) " +
            "FROM Category c " +
            "LEFT JOIN c.products p " +
            "WHERE c.id = :categoryId AND p.isActive = true")
    List<Object[]> getPriceRangeByCategory(@Param("categoryId") Long categoryId);

    // Total order count by category
    @Query("SELECT COUNT(DISTINCT o.id) " +
            "FROM Category c " +
            "LEFT JOIN c.products p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "LEFT JOIN oi.order o ON o.status = 'DELIVERED' " +
            "WHERE c.id = :categoryId")
    Long getTotalOrdersByCategory(@Param("categoryId") Long categoryId);

    // Check if category has products (for deletion validation)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId")
    boolean hasProducts(@Param("categoryId") Long categoryId);

    // Categories overview for admin
    @Query("SELECT c.id, c.name, " +
            "COUNT(p) as productCount, " +
            "COALESCE(SUM(oi.unitPrice * oi.quantity), 0) as revenue " +
            "FROM Category c " +
            "LEFT JOIN c.products p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "LEFT JOIN oi.order o ON o.status = 'DELIVERED' " +
            "GROUP BY c.id, c.name")
    List<Object[]> getCategoriesOverview();

    // Find fastest growing category (by order count in recent period)
    @Query("SELECT c.id, c.name, COUNT(o) as recentOrders " +
            "FROM Category c " +
            "LEFT JOIN c.products p " +
            "LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
            "LEFT JOIN oi.order o ON o.createdAt >= :startDate " +
            "GROUP BY c.id, c.name " +
            "ORDER BY recentOrders DESC")
    List<Object[]> getFastestGrowingCategories(@Param("startDate") java.time.LocalDateTime startDate);
}
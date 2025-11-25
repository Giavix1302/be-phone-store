package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Category;
import fit.se.be_phone_store.entity.Brand;
import fit.se.be_phone_store.entity.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductRepository interface for Product entity operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic finder methods
    Optional<Product> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    // Find active products
    List<Product> findByIsActiveTrue();
    
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    // Find by category
    List<Product> findByCategoryAndIsActiveTrue(Category category);
    
    Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);
    
    // Find by brand
    List<Product> findByBrandAndIsActiveTrue(Brand brand);
    
    Page<Product> findByBrandAndIsActiveTrue(Brand brand, Pageable pageable);
    
    // Find by color
    List<Product> findByColorAndIsActiveTrue(Color color);
    
    // Find by category name
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName AND p.isActive = true")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName AND p.isActive = true")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
    
    // Find by brand name
    @Query("SELECT p FROM Product p WHERE p.brand.name = :brandName AND p.isActive = true")
    List<Product> findByBrandName(@Param("brandName") String brandName);
    
    // Search products by name and description
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.isActive = true")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.isActive = true")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Find by price range
    @Query("SELECT p FROM Product p WHERE " +
           "COALESCE(p.discountPrice, p.price) BETWEEN :minPrice AND :maxPrice AND " +
           "p.isActive = true")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE " +
           "COALESCE(p.discountPrice, p.price) BETWEEN :minPrice AND :maxPrice AND " +
           "p.isActive = true")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice, 
                                  Pageable pageable);
    
    // Find products with discount
    @Query("SELECT p FROM Product p WHERE p.discountPrice IS NOT NULL AND p.discountPrice < p.price AND p.isActive = true")
    List<Product> findProductsWithDiscount();
    
    @Query("SELECT p FROM Product p WHERE p.discountPrice IS NOT NULL AND p.discountPrice < p.price AND p.isActive = true")
    Page<Product> findProductsWithDiscount(Pageable pageable);
    
    // Find in stock products
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findInStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    Page<Product> findInStockProducts(Pageable pageable);
    
    // Find out of stock products
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();
    
    // Find low stock products (stock <= threshold)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    // Advanced filtering
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:colorId IS NULL OR p.color.id = :colorId) AND " +
           "(:minPrice IS NULL OR COALESCE(p.discountPrice, p.price) >= :minPrice) AND " +
           "(:maxPrice IS NULL OR COALESCE(p.discountPrice, p.price) <= :maxPrice) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false)) AND " +
           "p.isActive = true")
    Page<Product> findProductsWithFilters(@Param("categoryId") Long categoryId,
                                         @Param("brandId") Long brandId,
                                         @Param("colorId") Long colorId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         @Param("inStock") Boolean inStock,
                                         Pageable pageable);
    
    // Find featured products (if you add isFeatured field later)
    // List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    
    // Find best selling products (based on order items)
    @Query("SELECT p, COUNT(oi) as orderCount FROM Product p " +
           "JOIN OrderItem oi ON p.id = oi.product.id " +
           "WHERE p.isActive = true " +
           "GROUP BY p " +
           "ORDER BY orderCount DESC")
    List<Object[]> findBestSellingProducts();
    
    // Find related products (same category, different product)
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.id != :productId AND p.isActive = true")
    List<Product> findRelatedProducts(@Param("category") Category category, 
                                     @Param("productId") Long productId, 
                                     Pageable pageable);
    
    // Find similar products (same brand and category)
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.brand = :brand AND p.id != :productId AND p.isActive = true")
    List<Product> findSimilarProducts(@Param("category") Category category,
                                     @Param("brand") Brand brand,
                                     @Param("productId") Long productId,
                                     Pageable pageable);
    
    // Statistical queries
    @Query("SELECT MIN(COALESCE(p.discountPrice, p.price)) FROM Product p WHERE p.isActive = true")
    BigDecimal findMinPrice();
    
    @Query("SELECT MAX(COALESCE(p.discountPrice, p.price)) FROM Product p WHERE p.isActive = true")
    BigDecimal findMaxPrice();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    long countActiveProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    long countInStockProducts();
    
    // Find newest products
    List<Product> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    
    // Count products by category
    @Query("SELECT p.category.name, COUNT(p) FROM Product p WHERE p.isActive = true GROUP BY p.category.name")
    List<Object[]> countProductsByCategory();
    
    // Count products by brand
    @Query("SELECT p.brand.name, COUNT(p) FROM Product p WHERE p.isActive = true GROUP BY p.brand.name")
    List<Object[]> countProductsByBrand();

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.color.id = :colorId")
    boolean existsByColorId(@Param("colorId") Long colorId);
}

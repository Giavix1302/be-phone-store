package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.ProductColor;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductColorRepository interface for ProductColor junction table operations
 */
@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {

    // Find all colors for a product
    List<ProductColor> findByProduct(Product product);
    
    List<ProductColor> findByProductId(Long productId);
    
    // Find all products with a specific color
    List<ProductColor> findByColor(Color color);
    
    List<ProductColor> findByColorId(Long colorId);
    
    // Check if product-color combination exists
    Optional<ProductColor> findByProductAndColor(Product product, Color color);
    
    Optional<ProductColor> findByProductIdAndColorId(Long productId, Long colorId);
    
    boolean existsByProductIdAndColorId(Long productId, Long colorId);
    
    // Delete by product and color
    void deleteByProductAndColor(Product product, Color color);
    
    void deleteByProductIdAndColorId(Long productId, Long colorId);
    
    // Delete all colors for a product
    void deleteByProduct(Product product);
    
    void deleteByProductId(Long productId);
    
    // Get available colors for specific product (returns Color entities)
    @Query("SELECT pc.color FROM ProductColor pc WHERE pc.product.id = :productId")
    List<Color> findColorsByProductId(@Param("productId") Long productId);
    
    // Get products available in specific color
    @Query("SELECT pc.product FROM ProductColor pc WHERE pc.color.id = :colorId AND pc.product.isActive = true")
    List<Product> findProductsByColorId(@Param("colorId") Long colorId);
    
    // Count available colors for product
    @Query("SELECT COUNT(pc) FROM ProductColor pc WHERE pc.product.id = :productId")
    long countColorsByProductId(@Param("productId") Long productId);
    
    // Count products available in color
    @Query("SELECT COUNT(pc) FROM ProductColor pc WHERE pc.color.id = :colorId")
    long countProductsByColorId(@Param("colorId") Long colorId);

    // In ProductColorRepository.java
    @Query("SELECT COUNT(pc) > 0 FROM ProductColor pc WHERE pc.color.id = :colorId")
    boolean existsByColorId(@Param("colorId") Long colorId);
}

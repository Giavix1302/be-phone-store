package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.ProductImage;
import fit.se.be_phone_store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductImageRepository interface for ProductImage entity operations
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Find all images for a product
    List<ProductImage> findByProduct(Product product);
    
    List<ProductImage> findByProductId(Long productId);
    
    // Find primary image for product
    Optional<ProductImage> findByProductAndIsPrimaryTrue(Product product);
    
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);
    
    // Find secondary images (non-primary)
    List<ProductImage> findByProductAndIsPrimaryFalse(Product product);
    
    List<ProductImage> findByProductIdAndIsPrimaryFalse(Long productId);
    
    // Find images ordered by primary first
    List<ProductImage> findByProductOrderByIsPrimaryDesc(Product product);
    
    List<ProductImage> findByProductIdOrderByIsPrimaryDesc(Long productId);
    
    // Check if product has primary image
    boolean existsByProductAndIsPrimaryTrue(Product product);
    
    boolean existsByProductIdAndIsPrimaryTrue(Long productId);
    
    // Count images for product
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
    
    // Delete all images for product
    void deleteByProduct(Product product);
    
    void deleteByProductId(Long productId);
    
    // Find by image URL
    Optional<ProductImage> findByImageUrl(String imageUrl);
    
    // Find products without images
    @Query("SELECT p FROM Product p WHERE p.id NOT IN (SELECT DISTINCT pi.product.id FROM ProductImage pi)")
    List<Product> findProductsWithoutImages();
    
    // Find products with multiple images
    @Query("SELECT pi.product FROM ProductImage pi GROUP BY pi.product HAVING COUNT(pi) > 1")
    List<Product> findProductsWithMultipleImages();
    
    // Get first image URL for product (primary if exists, otherwise first available)
    @Query("SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = :productId " +
           "ORDER BY pi.isPrimary DESC, pi.id ASC")
    List<String> findImageUrlsByProductId(@Param("productId") Long productId);
}

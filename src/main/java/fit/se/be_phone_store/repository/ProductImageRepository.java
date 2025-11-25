package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductImageRepository
 * FIXED: Added findByProductIdAndIsPrimaryTrue method to return List instead of Optional
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Find all images for a product
    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductId(Long productId);

    // Find images ordered by primary status (primary first)
    List<ProductImage> findByProductIdOrderByIsPrimaryDesc(Long productId);

    // Find single primary image (Optional)
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);

    // Check if product has images
    boolean existsByProductId(Long productId);

    // Count images for product
    long countByProductId(Long productId);

    // Delete all images for a product
    void deleteByProductId(Long productId);
}
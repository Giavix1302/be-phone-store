package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ColorRepository interface for Color entity operations
 */
@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    // Find by color name
    Optional<Color> findByColorName(String colorName);

    // Find by hex code
    Optional<Color> findByHexCode(String hexCode);

    // Check if color exists by name
    boolean existsByColorName(String colorName);

    // Check if color exists by hex code
    boolean existsByHexCode(String hexCode);

    // Search colors by name with pagination
    Page<Color> findByColorNameContainingIgnoreCaseOrderByColorNameAsc(String colorName, Pageable pageable);

    // Search colors by name
    @Query("SELECT c FROM Color c WHERE LOWER(c.colorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Color> searchByColorName(@Param("keyword") String keyword);

    // Find colors used in products
    @Query("SELECT DISTINCT c FROM Color c WHERE c.id IN (SELECT p.color.id FROM Product p)")
    List<Color> findColorsUsedInProducts();

    // Find colors available for specific product
    @Query("SELECT c FROM Color c JOIN ProductColor pc ON c.id = pc.color.id WHERE pc.product.id = :productId")
    List<Color> findColorsByProductId(@Param("productId") Long productId);

    // Count products by color (as default color)
    @Query("SELECT c, COUNT(p) FROM Color c LEFT JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Object[]> countProductsByColor();

    // Find popular colors (used in most products)
    @Query("SELECT c FROM Color c JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Color> findPopularColors();

    // Find all colors ordered by name
    List<Color> findAllByOrderByColorNameAsc();

    // Find all colors ordered by creation date
    List<Color> findAllByOrderByCreatedAtDesc();
}
package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BrandRepository interface for Brand entity operations
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    // Find by brand name
    Optional<Brand> findByName(String name);
    
    // Find by name case insensitive
    Optional<Brand> findByNameIgnoreCase(String name);
    
    // Check if brand exists
    boolean existsByName(String name);
    
    // Search brands by name
    @Query("SELECT b FROM Brand b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Brand> searchByName(@Param("keyword") String keyword);
    
    // Find brands with products
    @Query("SELECT DISTINCT b FROM Brand b WHERE b.id IN (SELECT p.brand.id FROM Product p)")
    List<Brand> findBrandsWithProducts();
    
    // Count products by brand
    @Query("SELECT b, COUNT(p) FROM Brand b LEFT JOIN b.products p GROUP BY b ORDER BY COUNT(p) DESC")
    List<Object[]> countProductsByBrand();
    
    // Find popular brands (top 10 by product count)
    @Query("SELECT b FROM Brand b JOIN b.products p GROUP BY b ORDER BY COUNT(p) DESC")
    List<Brand> findPopularBrands();
    
    // Find brands ordered by name
    List<Brand> findAllByOrderByNameAsc();
    
    // Find brands ordered by creation date
    List<Brand> findAllByOrderByCreatedAtDesc();
}

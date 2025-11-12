package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.ProductSpecification;
import fit.se.be_phone_store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductSpecificationRepository interface for ProductSpecification entity operations
 */
@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {

    // Find all specifications for a product
    List<ProductSpecification> findByProduct(Product product);
    
    List<ProductSpecification> findByProductId(Long productId);
    
    // Find specifications by name
    List<ProductSpecification> findBySpecName(String specName);
    
    // Find specifications by name and value
    List<ProductSpecification> findBySpecNameAndSpecValue(String specName, String specValue);
    
    // Find specifications for product by name
    Optional<ProductSpecification> findByProductAndSpecName(Product product, String specName);
    
    Optional<ProductSpecification> findByProductIdAndSpecName(Long productId, String specName);
    
    // Find products with specific specification
    @Query("SELECT ps.product FROM ProductSpecification ps WHERE ps.specName = :specName AND ps.specValue = :specValue")
    List<Product> findProductsBySpecification(@Param("specName") String specName, 
                                            @Param("specValue") String specValue);
    
    // Search products by specification value
    @Query("SELECT DISTINCT ps.product FROM ProductSpecification ps WHERE " +
           "ps.specName = :specName AND " +
           "LOWER(ps.specValue) LIKE LOWER(CONCAT('%', :value, '%'))")
    List<Product> searchProductsBySpecValue(@Param("specName") String specName, 
                                          @Param("value") String value);
    
    // Get all unique specification names
    @Query("SELECT DISTINCT ps.specName FROM ProductSpecification ps ORDER BY ps.specName")
    List<String> findAllSpecNames();
    
    // Get all unique values for a specification name
    @Query("SELECT DISTINCT ps.specValue FROM ProductSpecification ps WHERE ps.specName = :specName ORDER BY ps.specValue")
    List<String> findSpecValuesByName(@Param("specName") String specName);
    
    // Count specifications by product
    @Query("SELECT COUNT(ps) FROM ProductSpecification ps WHERE ps.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
    
    // Delete specifications by product
    void deleteByProduct(Product product);
    
    void deleteByProductId(Long productId);
    
    // Delete specific specification
    void deleteByProductAndSpecName(Product product, String specName);
    
    void deleteByProductIdAndSpecName(Long productId, String specName);
    
    // Find products with RAM specification
    @Query("SELECT ps.product FROM ProductSpecification ps WHERE ps.specName = 'RAM' AND ps.specValue = :ramValue")
    List<Product> findProductsByRAM(@Param("ramValue") String ramValue);
    
    // Find products with Storage specification
    @Query("SELECT ps.product FROM ProductSpecification ps WHERE ps.specName = 'Storage' AND ps.specValue = :storageValue")
    List<Product> findProductsByStorage(@Param("storageValue") String storageValue);
    
    // Find products with Screen Size specification
    @Query("SELECT ps.product FROM ProductSpecification ps WHERE ps.specName = 'Screen Size' AND ps.specValue = :screenSize")
    List<Product> findProductsByScreenSize(@Param("screenSize") String screenSize);
    
    // Advanced specification search
    @Query("SELECT ps.product FROM ProductSpecification ps WHERE " +
           "(:specName IS NULL OR ps.specName = :specName) AND " +
           "(:specValue IS NULL OR LOWER(ps.specValue) LIKE LOWER(CONCAT('%', :specValue, '%')))")
    List<Product> findProductsBySpecifications(@Param("specName") String specName,
                                             @Param("specValue") String specValue);
}

package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository interface for Category entity operations
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find by category name
    Optional<Category> findByName(Category.CategoryName name);
    
    // Check if category exists by name
    boolean existsByName(Category.CategoryName name);
    
    // Find categories with products
    @Query("SELECT DISTINCT c FROM Category c WHERE c.id IN (SELECT p.category.id FROM Product p)")
    List<Category> findCategoriesWithProducts();
    
    // Count products in each category
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c")
    List<Object[]> countProductsByCategory();
    
    // Find categories ordered by creation date
    List<Category> findAllByOrderByCreatedAtDesc();
}

package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.Review;
import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReviewRepository interface for Review entity operations
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    // Find reviews by product
    List<Review> findByProduct(Product product);
    
    List<Review> findByProductId(Long productId);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    // Find reviews by product with eager loading of user
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.id = :productId")
    List<Review> findByProductIdWithUser(@Param("productId") Long productId);
    
    // Find reviews by product and rating with eager loading of user
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.id = :productId AND r.rating = :rating")
    List<Review> findByProductIdAndRatingWithUser(@Param("productId") Long productId, @Param("rating") Integer rating);
    
    // Find reviews by user
    List<Review> findByUser(User user);
    
    List<Review> findByUserId(Long userId);
    
    // Find reviews by user with product loaded
    @Query("SELECT r FROM Review r JOIN FETCH r.product WHERE r.user.id = :userId")
    List<Review> findByUserIdWithProduct(@Param("userId") Long userId);
    
    // Find specific user review for product
    Optional<Review> findByUserAndProduct(User user, Product product);
    
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
    
    // Check if user has reviewed product
    boolean existsByUserAndProduct(User user, Product product);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Find reviews by rating
    List<Review> findByRating(Integer rating);
    
    List<Review> findByProductAndRating(Product product, Integer rating);
    
    List<Review> findByProductIdAndRating(Long productId, Integer rating);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductIdAndRating(Long productId, Integer rating, Pageable pageable);
    
    @EntityGraph(attributePaths = {"product"})
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.product " +
           "WHERE r.id = :id")
    Optional<Review> findByIdWithUserAndProduct(@Param("id") Long id);
    
    // Find reviews by rating range
    List<Review> findByRatingBetween(Integer minRating, Integer maxRating);
    
    List<Review> findByProductAndRatingBetween(Product product, Integer minRating, Integer maxRating);
    
    // Find positive reviews (4-5 stars)
    @Query("SELECT r FROM Review r WHERE r.rating >= 4")
    List<Review> findPositiveReviews();
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating >= 4")
    List<Review> findPositiveReviewsByProductId(@Param("productId") Long productId);
    
    // Find negative reviews (1-2 stars)
    @Query("SELECT r FROM Review r WHERE r.rating <= 2")
    List<Review> findNegativeReviews();
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating <= 2")
    List<Review> findNegativeReviewsByProductId(@Param("productId") Long productId);
    
    // Find reviews with comments
    @Query("SELECT r FROM Review r WHERE r.comment IS NOT NULL AND LENGTH(r.comment) > 0")
    List<Review> findReviewsWithComments();
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.comment IS NOT NULL AND LENGTH(r.comment) > 0")
    List<Review> findReviewsWithCommentsByProductId(@Param("productId") Long productId);
    
    // Calculate average rating for product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);
    
    // Count reviews by rating for product
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> countReviewsByRatingForProduct(@Param("productId") Long productId);
    
    // Count total reviews for product
    long countByProduct(Product product);
    
    long countByProductId(Long productId);
    
    long countByUserId(Long userId);

    long countByProductIdAndRatingGreaterThanEqual(Long productId, Integer rating);

    long countByProductIdAndCreatedAtBetween(Long productId, LocalDateTime from, LocalDateTime to);
    
    // Find recent reviews
    List<Review> findByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(Pageable pageable);
    
    // Find reviews by date range
    List<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find reviews ordered by rating (highest first)
    List<Review> findByProductOrderByRatingDesc(Product product);
    
    List<Review> findByProductIdOrderByRatingDesc(Long productId);
    
    // Find reviews ordered by date (newest first)
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    // Find products with highest average ratings
    @Query("SELECT r.product, AVG(r.rating) as avgRating FROM Review r GROUP BY r.product ORDER BY avgRating DESC")
    List<Object[]> findProductsByAverageRating();
    
    // Find products with most reviews
    @Query("SELECT r.product, COUNT(r) as reviewCount FROM Review r GROUP BY r.product ORDER BY reviewCount DESC")
    List<Object[]> findProductsByReviewCount();
    
    // Find users who write most reviews
    @Query("SELECT r.user, COUNT(r) as reviewCount FROM Review r GROUP BY r.user ORDER BY reviewCount DESC")
    List<Object[]> findMostActiveReviewers();
    
    // Search reviews by comment content
    @Query("SELECT r FROM Review r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Review> searchReviewsByComment(@Param("keyword") String keyword);
    
    // Find reviews for products in specific category
    @Query("SELECT r FROM Review r WHERE r.product.category.name = :categoryName")
    List<Review> findReviewsByCategoryName(@Param("categoryName") String categoryName);
    
    // Find reviews for products from specific brand
    @Query("SELECT r FROM Review r WHERE r.product.brand.name = :brandName")
    List<Review> findReviewsByBrandName(@Param("brandName") String brandName);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.user.id = :userId")
    Double calculateAverageRatingByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT AVG(r.rating) FROM Review r 
        WHERE r.product.id = :productId 
          AND r.createdAt BETWEEN :from AND :to
    """)
    Double calculateAverageRatingByProductIdAndDateRange(@Param("productId") Long productId,
                                                         @Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);
    
    // Get review statistics for product
    @Query("SELECT " +
           "COUNT(r) as totalReviews, " +
           "AVG(r.rating) as averageRating, " +
           "COUNT(CASE WHEN r.rating = 5 THEN 1 END) as fiveStars, " +
           "COUNT(CASE WHEN r.rating = 4 THEN 1 END) as fourStars, " +
           "COUNT(CASE WHEN r.rating = 3 THEN 1 END) as threeStars, " +
           "COUNT(CASE WHEN r.rating = 2 THEN 1 END) as twoStars, " +
           "COUNT(CASE WHEN r.rating = 1 THEN 1 END) as oneStar " +
           "FROM Review r WHERE r.product.id = :productId")
    Object[] getReviewStatisticsByProductId(@Param("productId") Long productId);
    
    // Delete reviews by user
    void deleteByUser(User user);
    
    void deleteByUserId(Long userId);
    
    // Delete reviews by product
    void deleteByProduct(Product product);
    
    void deleteByProductId(Long productId);

    @Query("""
        SELECT 
            COUNT(r) as totalReviews,
            AVG(r.rating) as avgRating,
            COUNT(DISTINCT r.product.id) as productCount,
            COUNT(DISTINCT r.user.id) as reviewerCount
        FROM Review r
        WHERE (:productId IS NULL OR r.product.id = :productId)
          AND r.createdAt BETWEEN :from AND :to
    """)
    List<Object[]> getAdminOverviewStats(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         @Param("productId") Long productId);

    @Query("""
        SELECT 
            r.rating, COUNT(r)
        FROM Review r
        WHERE (:productId IS NULL OR r.product.id = :productId)
          AND r.createdAt BETWEEN :from AND :to
        GROUP BY r.rating
    """)
    List<Object[]> getAdminRatingDistribution(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("productId") Long productId);

    @Query("""
        SELECT 
            FUNCTION('DATE', r.createdAt) as reviewDate,
            COUNT(r),
            AVG(r.rating)
        FROM Review r
        WHERE (:productId IS NULL OR r.product.id = :productId)
          AND r.createdAt BETWEEN :from AND :to
        GROUP BY FUNCTION('DATE', r.createdAt)
        ORDER BY reviewDate
    """)
    List<Object[]> getAdminDailyStats(@Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to,
                                      @Param("productId") Long productId);

    @Query("""
        SELECT 
            r.product.id,
            r.product.name,
            AVG(r.rating),
            COUNT(r)
        FROM Review r
        WHERE (:productId IS NULL OR r.product.id = :productId)
          AND r.createdAt BETWEEN :from AND :to
        GROUP BY r.product.id, r.product.name
        ORDER BY AVG(r.rating) DESC, COUNT(r) DESC
    """)
    List<Object[]> findTopRatedProductsForAdmin(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to,
                                                @Param("productId") Long productId,
                                                Pageable pageable);

    @Query("""
        SELECT 
            r.user.id,
            r.user.fullName,
            COUNT(r),
            AVG(r.rating)
        FROM Review r
        WHERE (:productId IS NULL OR r.product.id = :productId)
          AND r.createdAt BETWEEN :from AND :to
        GROUP BY r.user.id, r.user.fullName
        ORDER BY COUNT(r) DESC, AVG(r.rating) DESC
    """)
    List<Object[]> findMostActiveReviewersForAdmin(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to,
                                                   @Param("productId") Long productId,
                                                   Pageable pageable);

    /**
     * Check if user has reviewed product after a specific date
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM Review r
        WHERE r.user.id = :userId
          AND r.product.id = :productId
          AND r.createdAt >= :afterDate
    """)
    boolean existsByUserIdAndProductIdAndCreatedAtAfter(@Param("userId") Long userId,
                                                         @Param("productId") Long productId,
                                                         @Param("afterDate") LocalDateTime afterDate);
}

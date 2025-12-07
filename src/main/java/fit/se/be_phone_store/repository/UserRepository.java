package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finder methods
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Find by role
    List<User> findByRole(User.Role role);
    
    // Find active users
    List<User> findByEnabledTrue();
    
    // Find by phone
    Optional<User> findByPhone(String phone);
    
    // Search users by name or email
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    // Count users by role
    long countByRole(User.Role role);
    
    // Find users with orders
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (SELECT o.user.id FROM Order o)")
    List<User> findUsersWithOrders();
    
    // Find users with reviews
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (SELECT r.user.id FROM Review r)")
    List<User> findUsersWithReviews();
    
    // Find users with filters (Admin)
    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
          AND (:enabled IS NULL OR u.enabled = :enabled)
          AND (:fromDate IS NULL OR u.createdAt >= :fromDate)
          AND (:toDate IS NULL OR u.createdAt <= :toDate)
        """)
    List<User> findUsersWithFilters(
        @Param("role") User.Role role,
        @Param("enabled") Boolean enabled,
        @Param("fromDate") java.time.LocalDateTime fromDate,
        @Param("toDate") java.time.LocalDateTime toDate
    );
    
    long countByEnabled(Boolean enabled);
    
    // Count users by date range
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get daily registrations
    @Query("""
        SELECT FUNCTION('DATE', u.createdAt) as registrationDate, COUNT(u) as userCount
        FROM User u
        WHERE u.createdAt BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('DATE', u.createdAt)
        ORDER BY registrationDate ASC
        """)
    List<Object[]> getDailyRegistrations(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find users created after date
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    // Find users created between dates
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Get weekly user registrations for charts
    @Query("""
        SELECT 
            YEAR(u.createdAt) as year,
            WEEK(u.createdAt) as week,
            COUNT(u) as newUsers,
            (SELECT COUNT(u2) FROM User u2 WHERE u2.createdAt <= MAX(u.createdAt)) as totalUsers
        FROM User u
        WHERE u.createdAt BETWEEN :startDate AND :endDate
        GROUP BY YEAR(u.createdAt), WEEK(u.createdAt)
        ORDER BY year ASC, week ASC
        """)
    List<Object[]> getWeeklyUserGrowth(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}

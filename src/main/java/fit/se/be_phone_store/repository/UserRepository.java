package fit.se.be_phone_store.repository;

import fit.se.be_phone_store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

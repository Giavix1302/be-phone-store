package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.entity.Order;
import fit.se.be_phone_store.entity.Review;
import fit.se.be_phone_store.entity.OrderItem;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.repository.UserRepository;
import fit.se.be_phone_store.repository.OrderRepository;
import fit.se.be_phone_store.repository.ReviewRepository;
import fit.se.be_phone_store.repository.OrderItemRepository;
import fit.se.be_phone_store.dto.request.UpdateProfileRequest;
import fit.se.be_phone_store.dto.request.ValidateProfileRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.UserProfileResponse;
import fit.se.be_phone_store.dto.response.UserStatisticsResponse;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import fit.se.be_phone_store.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * UserService - Handles user management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Get user profile by ID
     * @param userId User ID
     * @return API response with user profile
     */
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getUserProfile(Long userId) {
        log.info("Getting user profile for ID: {}", userId);

        // Check if current user can access this profile
        if (!authService.canAccessResource(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfileResponse profile = mapToUserProfileResponse(user);
        return ApiResponse.success("User profile retrieved successfully", profile);
    }

    /**
     * Get current user profile
     * @return API response with current user profile
     */
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        User currentUser = authService.getCurrentUser();
        UserProfileResponse profile = mapToUserProfileResponse(currentUser);
        return ApiResponse.success("Profile retrieved successfully", profile);
    }

    /**
     * Update user profile
     * @param userId User ID
     * @param request Update profile request
     * @return API response
     */
    public ApiResponse<UserProfileResponse> updateUserProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating user profile for ID: {}", userId);

        // Check if current user can update this profile
        if (!authService.canAccessResource(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update profile fields
        if (request.getFull_name() != null) {
            user.setFullName(request.getFull_name());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        User updatedUser = userRepository.save(user);
        UserProfileResponse profile = mapToUserProfileResponse(updatedUser);

        log.info("User profile updated successfully: {}", userId);
        return ApiResponse.success("Profile updated successfully", profile);
    }

    /**
     * Get all users (Admin only)
     * @param pageable Pagination parameters
     * @return Paged API response with users
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<UserProfileResponse> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination");

        // Check if current user is admin
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Page<User> usersPage = userRepository.findAll(pageable);
        Page<UserProfileResponse> profilesPage = usersPage.map(this::mapToUserProfileResponse);

        return PagedApiResponse.success("Users retrieved successfully", profilesPage);
    }

    /**
     * Search users by keyword (Admin only)
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Paged API response with users
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<UserProfileResponse> searchUsers(String keyword, Pageable pageable) {
        log.info("Searching users with keyword: {}", keyword);

        // Check if current user is admin
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        List<User> users = userRepository.searchUsers(keyword);
        List<UserProfileResponse> profiles = users.stream()
            .map(this::mapToUserProfileResponse)
            .collect(Collectors.toList());

        // Create simple pagination for search results
        PagedApiResponse.PaginationInfo pagination = PagedApiResponse.PaginationInfo.builder()
            .currentPage(0)
            .pageSize(profiles.size())
            .totalElements(profiles.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        return PagedApiResponse.success("Search results retrieved successfully", profiles, pagination);
    }

    /**
     * Get users by role (Admin only)
     * @param role User role
     * @param pageable Pagination parameters
     * @return Paged API response with users
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<UserProfileResponse> getUsersByRole(User.Role role, Pageable pageable) {
        log.info("Getting users by role: {}", role);

        // Check if current user is admin
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        List<User> users = userRepository.findByRole(role);
        List<UserProfileResponse> profiles = users.stream()
            .map(this::mapToUserProfileResponse)
            .collect(Collectors.toList());

        // Create simple pagination
        PagedApiResponse.PaginationInfo pagination = PagedApiResponse.PaginationInfo.builder()
            .currentPage(0)
            .pageSize(profiles.size())
            .totalElements(profiles.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        return PagedApiResponse.success("Users retrieved successfully", profiles, pagination);
    }

    /**
     * Enable/Disable user (Admin only)
     * @param userId User ID
     * @param enabled Enable status
     * @return API response
     */
    public ApiResponse<Void> setUserEnabled(Long userId, boolean enabled) {
        log.info("Setting user {} enabled status to: {}", userId, enabled);

        // Check if current user is admin
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(enabled);
        userRepository.save(user);

        String message = enabled ? "User enabled successfully" : "User disabled successfully";
        log.info("User {} enabled status changed to: {}", userId, enabled);
        return ApiResponse.success(message);
    }

    /**
     * Update user role (Admin only)
     * @param userId User ID
     * @param role New role
     * @return API response
     */
    public ApiResponse<Void> updateUserRole(Long userId, User.Role role) {
        log.info("Updating user {} role to: {}", userId, role);

        // Check if current user is admin
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(role);
        userRepository.save(user);

        log.info("User {} role updated to: {}", userId, role);
        return ApiResponse.success("User role updated successfully");
    }

    /**
     * Get user statistics (Admin only)
     * @return API response with user statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        log.info("Getting user statistics");

        // Check if current user is admin
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", userRepository.count());
        statistics.put("totalAdmins", userRepository.countByRole(User.Role.ADMIN));
        statistics.put("totalCustomers", userRepository.countByRole(User.Role.USER));
        statistics.put("enabledUsers", userRepository.findByEnabledTrue().size());
        statistics.put("usersWithOrders", userRepository.findUsersWithOrders().size());
        statistics.put("usersWithReviews", userRepository.findUsersWithReviews().size());

        return ApiResponse.success("User statistics retrieved successfully", statistics);
    }

    /**
     * Delete user account (Admin only or own account)
     * @param userId User ID
     * @return API response
     */
    public ApiResponse<Void> deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        // Check if current user can delete this account
        if (!authService.canAccessResource(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Soft delete - disable the user instead of hard delete
        user.setEnabled(false);
        userRepository.save(user);

        log.info("User {} deleted (disabled) successfully", userId);
        return ApiResponse.success("User account deleted successfully");
    }

    /**
     * Check if username is available
     * @param username Username to check
     * @return API response with availability status
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Boolean>> checkUsernameAvailability(String username) {
        boolean available = !userRepository.existsByUsername(username);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        
        String message = available ? "Username is available" : "Username is already taken";
        return ApiResponse.success(message, result);
    }

    /**
     * Check if email is available
     * @param email Email to check
     * @return API response with availability status
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Boolean>> checkEmailAvailability(String email) {
        boolean available = !userRepository.existsByEmail(email);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        
        String message = available ? "Email is available" : "Email is already registered";
        return ApiResponse.success(message, result);
    }

    /**
     * Update current user profile
     * @param request Update profile request
     * @return API response
     */
    public ApiResponse<UserProfileResponse> updateCurrentUserProfile(UpdateProfileRequest request) {
        User currentUser = authService.getCurrentUser();
        return updateUserProfile(currentUser.getId(), request);
    }

    /**
     * Update current user profile (partial update)
     * @param request Update profile request
     * @return API response
     */
    public ApiResponse<UserProfileResponse> updateCurrentUserProfilePartial(UpdateProfileRequest request) {
        User currentUser = authService.getCurrentUser();
        return updateUserProfile(currentUser.getId(), request);
    }

    /**
     * Get current user statistics
     * @return API response with user statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<UserStatisticsResponse> getCurrentUserStatistics() {
        User currentUser = authService.getCurrentUser();
        Long userId = currentUser.getId();

        // Get all orders for user
        List<Order> orders = orderRepository.findByUserId(userId);
        List<Review> reviews = reviewRepository.findByUserId(userId);

        // Calculate account summary
        int totalOrders = orders.size();
        int completedOrders = (int) orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .count();
        int cancelledOrders = (int) orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
                .count();
        
        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UserStatisticsResponse.AccountSummary accountSummary = 
            new UserStatisticsResponse.AccountSummary(
                currentUser.getCreatedAt(),
                totalOrders,
                completedOrders,
                cancelledOrders,
                totalSpent,
                reviews.size()
            );

        // Calculate order statistics
        int pendingOrders = (int) orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                .count();
        int processingOrders = (int) orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PROCESSING)
                .count();
        int shippedOrders = (int) orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED)
                .count();

        UserStatisticsResponse.OrderStatistics orderStatistics = 
            new UserStatisticsResponse.OrderStatistics(
                pendingOrders,
                processingOrders,
                shippedOrders,
                completedOrders
            );

        // Get recent activity
        Order lastOrder = orders.stream()
                .max((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()))
                .orElse(null);

        Review lastReview = reviews.stream()
                .max((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()))
                .orElse(null);

        UserStatisticsResponse.LastOrder lastOrderInfo = null;
        if (lastOrder != null) {
            lastOrderInfo = new UserStatisticsResponse.LastOrder(
                lastOrder.getOrderNumber(),
                lastOrder.getTotalAmount(),
                lastOrder.getStatus().name(),
                lastOrder.getCreatedAt()
            );
        }

        UserStatisticsResponse.LastReview lastReviewInfo = null;
        if (lastReview != null && lastReview.getProduct() != null) {
            lastReviewInfo = new UserStatisticsResponse.LastReview(
                lastReview.getProduct().getName(),
                lastReview.getRating(),
                lastReview.getCreatedAt()
            );
        }

        UserStatisticsResponse.RecentActivity recentActivity = 
            new UserStatisticsResponse.RecentActivity(lastOrderInfo, lastReviewInfo);

        // Find favorite category
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                Product product = item.getProduct();
                if (product.getCategory() != null) {
                    String categoryName = product.getCategory().getName().name();
                    categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0) + 1);
                }
            }
        }

        UserStatisticsResponse.FavoriteCategory favoriteCategory = null;
        if (!categoryCount.isEmpty()) {
            String favoriteCategoryName = Collections.max(categoryCount.entrySet(), 
                    Map.Entry.comparingByValue()).getKey();
            Integer orderCount = categoryCount.get(favoriteCategoryName);
            favoriteCategory = new UserStatisticsResponse.FavoriteCategory(favoriteCategoryName, orderCount);
        }

        UserStatisticsResponse statistics = UserStatisticsResponse.builder()
                .user_id(userId)
                .account_summary(accountSummary)
                .order_statistics(orderStatistics)
                .recent_activity(recentActivity)
                .favorite_category(favoriteCategory)
                .build();

        return ApiResponse.success("Lấy thống kê user thành công", statistics);
    }

    /**
     * Validate profile data
     * @param request Validation request
     * @return API response with validation result
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> validateProfileData(ValidateProfileRequest request) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        Map<String, Boolean> validatedFields = new HashMap<>();

        boolean isValid = true;

        // Validate full name
        if (request.getFull_name() != null) {
            if (request.getFull_name().length() < 2 || request.getFull_name().length() > 100) {
                errors.put("full_name", "Họ tên phải có từ 2 đến 100 ký tự");
                validatedFields.put("full_name", false);
                isValid = false;
            } else if (!request.getFull_name().matches("^[\\p{L}\\s'-]+$")) {
                errors.put("full_name", "Họ tên không được chứa ký tự đặc biệt");
                validatedFields.put("full_name", false);
                isValid = false;
            } else {
                validatedFields.put("full_name", true);
            }
        }

        // Validate phone
        if (request.getPhone() != null) {
            if (!request.getPhone().matches("^0[0-9]{9}$")) {
                errors.put("phone", "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0");
                validatedFields.put("phone", false);
                isValid = false;
            } else {
                validatedFields.put("phone", true);
            }
        }

        // Validate address
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            if (request.getAddress().length() < 10 || request.getAddress().length() > 500) {
                errors.put("address", "Địa chỉ phải có từ 10 đến 500 ký tự");
                validatedFields.put("address", false);
                isValid = false;
            } else {
                validatedFields.put("address", true);
            }
        }

        result.put("is_valid", isValid);
        if (isValid) {
            result.put("validated_fields", validatedFields);
        } else {
            result.put("errors", errors);
        }

        String message = isValid ? "Dữ liệu hợp lệ" : "Dữ liệu không hợp lệ";
        return ApiResponse.success(message, result);
    }

    /**
     * Map User entity to UserProfileResponse DTO
     * @param user User entity
     * @return UserProfileResponse DTO
     */
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .address(user.getAddress())
            .avatar(user.getAvatar())
            .role(user.getRole().name())
            .enabled(user.getEnabled())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}

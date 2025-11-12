package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.repository.UserRepository;
import fit.se.be_phone_store.dto.request.LoginRequest;
import fit.se.be_phone_store.dto.request.RegisterRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.LoginResponse;
import fit.se.be_phone_store.exception.AuthenticationException;
import fit.se.be_phone_store.exception.UserAlreadyExistsException;
import fit.se.be_phone_store.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthService - Handles authentication and authorization business logic
 * NO LOMBOK DEPENDENCY - USING EXPLICIT LOGGING
 */
@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Register new user
     * @param request Registration request data
     * @return API response with user data
     */
    public ApiResponse<Map<String, Object>> register(RegisterRequest request) {
        System.out.println("Attempting to register user: " + request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            System.out.println("Registration failed - username already exists: " + request.getUsername());
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("Registration failed - email already exists: " + request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        System.out.println("User registered successfully: " + savedUser.getUsername());

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", savedUser.getId());
        responseData.put("username", savedUser.getUsername());
        responseData.put("email", savedUser.getEmail());
        responseData.put("fullName", savedUser.getFullName());

        return ApiResponse.success("User registered successfully", responseData);
    }

    /**
     * Login user and generate JWT token
     * @param request Login request data
     * @return API response with login data including JWT token
     */
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        System.out.println("Attempting login for user: " + request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // Generate JWT token
            String token = jwtUtils.generateJwtToken(authentication);

            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Create login response
            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .build();

            System.out.println("User logged in successfully: " + user.getUsername());
            return ApiResponse.success("Login successful", loginResponse);

        } catch (Exception e) {
            System.err.println("Login failed for user: " + request.getUsername() + " - " + e.getMessage());
            throw new AuthenticationException("Invalid username or password");
        }
    }

    /**
     * Get current authenticated user
     * @return Current user
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    /**
     * Get current user ID
     * @return Current user ID
     */
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if current user is admin
     * @return true if current user is admin
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUserAdmin() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRole() == User.Role.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate if user can access resource
     * @param resourceUserId User ID who owns the resource
     * @return true if user can access resource
     */
    @Transactional(readOnly = true)
    public boolean canAccessResource(Long resourceUserId) {
        try {
            User currentUser = getCurrentUser();
            // Admin can access all resources, users can only access their own
            return currentUser.getRole() == User.Role.ADMIN ||
                    currentUser.getId().equals(resourceUserId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Change user password
     * @param currentPassword Current password
     * @param newPassword New password
     * @return API response
     */
    public ApiResponse<Void> changePassword(String currentPassword, String newPassword) {
        User currentUser = getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);

        System.out.println("Password changed successfully for user: " + currentUser.getUsername());
        return ApiResponse.success("Password changed successfully");
    }

    /**
     * Logout user (invalidate token on client side)
     * @return API response
     */
    public ApiResponse<Void> logout() {
        User currentUser = getCurrentUser();
        System.out.println("User logged out: " + currentUser.getUsername());

        // Clear security context
        SecurityContextHolder.clearContext();

        return ApiResponse.success("Logout successful");
    }

    /**
     * Refresh JWT token
     * @param token Current token
     * @return API response with new token
     */
    public ApiResponse<Map<String, String>> refreshToken(String token) {
        try {
            // Validate current token
            if (!jwtUtils.validateJwtToken(token)) {
                throw new AuthenticationException("Invalid token");
            }

            // Get username from token
            String username = jwtUtils.getUsernameFromJwtToken(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // Generate new token
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            String newToken = jwtUtils.generateJwtToken(authentication);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("token", newToken);
            responseData.put("type", "Bearer");

            return ApiResponse.success("Token refreshed successfully", responseData);

        } catch (Exception e) {
            System.err.println("Token refresh failed: " + e.getMessage());
            throw new AuthenticationException("Token refresh failed");
        }
    }
}
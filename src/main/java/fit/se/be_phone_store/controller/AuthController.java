package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.*;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.LoginResponse;
import fit.se.be_phone_store.dto.response.UserProfileResponse;
import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.exception.AuthenticationException;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.repository.UserRepository;
import fit.se.be_phone_store.service.AuthService;
import fit.se.be_phone_store.service.EmailVerificationService;
import fit.se.be_phone_store.service.UserService;
import fit.se.be_phone_store.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles authentication and authorization endpoints
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * User Registration (Signup)
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@Valid @RequestBody SignupRequest request) {
        // Convert SignupRequest to RegisterRequest format
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(request.getEmail());
        registerRequest.setPassword(request.getPassword());
        registerRequest.setFullName(request.getFull_name());
        registerRequest.setPhone(request.getPhone());
        registerRequest.setAddress(request.getAddress());
        // Generate username from email
        registerRequest.setUsername(request.getEmail().split("@")[0]);

        ApiResponse<Map<String, Object>> response = authService.register(registerRequest);
        
        // Format response according to API spec
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("email", request.getEmail());
        responseData.put("verification_required", true);
        
        ApiResponse<Map<String, Object>> formattedResponse = ApiResponse.success(
            "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
            responseData
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(formattedResponse);
    }

    /**
     * Email Verification
     * POST /api/auth/verify-email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        // Verify the code
        boolean verified = emailVerificationService.verifyCode(request.getEmail(), request.getVerification_code());
        
        if (!verified) {
            throw new AuthenticationException("Mã xác thực không đúng hoặc đã hết hạn");
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("email", request.getEmail());
        responseData.put("verified", true);
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            "Xác thực email thành công. Tài khoản đã được kích hoạt.",
            responseData
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * User Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {
        
        ApiResponse<Map<String, Object>> response = authService.login(request);
        
        // Extract login response and refresh token from service response
        Map<String, Object> data = response.getData();
        LoginResponse loginResponse = (LoginResponse) data.get("loginResponse");
        String refreshToken = (String) data.get("refreshToken");
        
        // Set refresh token as httpOnly cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/api");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
        httpResponse.addCookie(refreshTokenCookie);
        
        // Return formatted response
        ApiResponse<LoginResponse> formattedResponse = ApiResponse.success(
            response.getMessage(),
            loginResponse
        );
        
        return ResponseEntity.ok(formattedResponse);
    }

    /**
     * Refresh Token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse httpResponse) {
        
        // Get refresh token from cookie
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null || !jwtUtils.validateRefreshToken(refreshToken)) {
            throw new fit.se.be_phone_store.exception.AuthenticationException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
   
        String email = jwtUtils.getUsernameFromJwtToken(refreshToken);
        
        String newAccessToken = jwtUtils.generateTokenFromUsername(email);
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("access_token", newAccessToken);
        
        ApiResponse<Map<String, String>> response = ApiResponse.success(
            "Làm mới token thành công",
            responseData
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * User Logout
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse httpResponse) {
        ApiResponse<Void> response = authService.logout();
        
        // Clear refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/api");
        refreshTokenCookie.setMaxAge(0); // Delete cookie
        httpResponse.addCookie(refreshTokenCookie);
        
        Map<String, Object> emptyData = new HashMap<>();
        ApiResponse<Void> formattedResponse = ApiResponse.success("Đăng xuất thành công", null);
        
        return ResponseEntity.ok(formattedResponse);
    }

    /**
     * Resend Verification Code
     * POST /api/auth/resend-verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
        
        if (existingUser.getEnabled()) {
            throw new BadRequestException("Tài khoản đã được xác thực");
        }

        emailVerificationService.generateVerificationCode(request.getEmail());
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("email", request.getEmail());
        responseData.put("sent_at", java.time.LocalDateTime.now().toString());
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            "Mã xác thực đã được gửi lại",
            responseData
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        ApiResponse<UserProfileResponse> response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        ApiResponse<Void> response = authService.changePassword(
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(response);
    }
}

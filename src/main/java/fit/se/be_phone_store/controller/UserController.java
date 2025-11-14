package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.UpdateProfileRequest;
import fit.se.be_phone_store.dto.request.ChangePasswordRequest;
import fit.se.be_phone_store.dto.request.ValidateProfileRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.UserProfileResponse;
import fit.se.be_phone_store.dto.response.UserStatisticsResponse;
import fit.se.be_phone_store.exception.AuthenticationException;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.service.UserService;
import fit.se.be_phone_store.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController - Handles user profile management endpoints
 */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Get current user profile
     * GET /api/users/me
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        log.info("Getting current user profile");
        ApiResponse<UserProfileResponse> response = userService.getCurrentUserProfile();
        response.setMessage("Lấy thông tin profile thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile (full update)
     * PUT /api/users/me
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating current user profile");
        ApiResponse<UserProfileResponse> response = userService.updateCurrentUserProfile(request);
        response.setMessage("Cập nhật profile thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile (partial update)
     * PATCH /api/users/me
     */
    @PatchMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfilePartial(
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Partially updating current user profile");
        ApiResponse<UserProfileResponse> response = userService.updateCurrentUserProfilePartial(request);
        response.setMessage("Cập nhật thông tin thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     * POST /api/users/me/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for current user");
        try {
            ApiResponse<Map<String, Object>> response = authService.changePassword(
                    request.getOld_password(),
                    request.getNew_password()
            );
            response.setMessage("Thay đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Get user statistics
     * GET /api/users/me/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getStatistics() {
        log.info("Getting statistics for current user");
        ApiResponse<UserStatisticsResponse> response = userService.getCurrentUserStatistics();
        response.setMessage("Lấy thống kê user thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Validate profile data
     * POST /api/users/me/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateProfile(
            @Valid @RequestBody ValidateProfileRequest request) {
        log.info("Validating profile data for current user");
        ApiResponse<Map<String, Object>> response = userService.validateProfileData(request);
        
        // Format response message based on validation result
        Map<String, Object> data = response.getData();
        Boolean isValid = (Boolean) data.get("is_valid");
        if (!isValid) {
            response.setSuccess(false);
            response.setMessage("Dữ liệu không hợp lệ");
        } else {
            response.setMessage("Dữ liệu hợp lệ");
        }
        
        return ResponseEntity.ok(response);
    }
}


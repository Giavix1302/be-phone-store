package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.AdminUserListResponse;
import fit.se.be_phone_store.dto.response.AdminUserDetailResponse;
import fit.se.be_phone_store.dto.response.UpdateUserStatusResponse;
import fit.se.be_phone_store.dto.response.UserOrderHistoryResponse;
import fit.se.be_phone_store.dto.response.UserStatisticsAdminResponse;
import fit.se.be_phone_store.dto.request.UpdateUserStatusRequest;
import fit.se.be_phone_store.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * AdminUserController 
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    /**
     * Get all users (Admin)
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResponse>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date,
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order) {
        log.info("Getting all users (Admin) - page: {}, limit: {}, role: {}, enabled: {}", 
                page, limit, role, enabled);

        ApiResponse<AdminUserListResponse> response = userService.getAllUsersAdmin(
                page, limit, role, enabled, from_date, to_date, sort_by, sort_order);
        response.setMessage("Lấy danh sách users thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get user detail (Admin)
     * GET /api/admin/users/{user_id}
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(
            @PathVariable("user_id") Long userId) {
        log.info("Getting user detail (Admin) for user ID: {}", userId);

        ApiResponse<AdminUserDetailResponse> response = userService.getUserDetailAdmin(userId);
        response.setMessage("Lấy chi tiết user thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Update user status (Admin)
     * PATCH /api/admin/users/{user_id}/status
     */
    @PatchMapping("/{user_id}/status")
    public ResponseEntity<ApiResponse<UpdateUserStatusResponse>> updateUserStatus(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("Updating user status (Admin) for user ID: {}, enabled: {}", userId, request.getEnabled());

        ApiResponse<UpdateUserStatusResponse> response = userService.updateUserStatus(userId, request);
        response.setMessage("Cập nhật trạng thái user thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get user order history (Admin)
     * GET /api/admin/users/{user_id}/orders
     */
    @GetMapping("/{user_id}/orders")
    public ResponseEntity<ApiResponse<UserOrderHistoryResponse>> getUserOrderHistory(
            @PathVariable("user_id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status) {
        log.info("Getting user order history (Admin) for user ID: {}, page: {}, limit: {}, status: {}", 
                userId, page, limit, status);

        ApiResponse<UserOrderHistoryResponse> response = userService.getUserOrderHistory(userId, page, limit, status);
        response.setMessage("Lấy lịch sử đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics (Admin)
     * GET /api/admin/users/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserStatisticsAdminResponse>> getUserStatistics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date) {
        log.info("Getting user statistics (Admin) - period: {}, from_date: {}, to_date: {}", 
                period, from_date, to_date);

        ApiResponse<UserStatisticsAdminResponse> response = userService.getUserStatisticsAdmin(period, from_date, to_date);
        response.setMessage("Lấy thống kê users thành công");
        return ResponseEntity.ok(response);
    }
}


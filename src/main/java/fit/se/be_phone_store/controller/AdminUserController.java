package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.AdminUserListResponse;
import fit.se.be_phone_store.service.UserService;
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
}


package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.color.CreateColorRequest;
import fit.se.be_phone_store.dto.request.color.UpdateColorAdminRequest;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.dto.response.color.ColorDetailResponse;
import fit.se.be_phone_store.dto.response.color.ColorResponse;
import fit.se.be_phone_store.dto.response.color.ColorStatisticsResponse;
import fit.se.be_phone_store.dto.response.color.ColorUsageResponse;
import fit.se.be_phone_store.service.ColorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ColorController - Handles color-related endpoints for both users and admins
 */
@RestController
@RequestMapping("/api") // QUAN TRỌNG: Thêm base mapping
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Colors", description = "Color management APIs")
public class ColorController {

    private final ColorService colorService;

    // ==================== USER APIS ====================

    /**
     * Get all colors for users
     * GET /api/colors
     */
    @GetMapping("/colors")
    @Operation(summary = "Get all colors", description = "Lấy danh sách tất cả colors")
    public ResponseEntity<ApiResponse<List<ColorResponse>>> getAllColors() {
        log.info("User API: Getting all colors");
        ApiResponse<List<ColorResponse>> response = colorService.getAllColors();
        return ResponseEntity.ok(response);
    }

    /**
     * Get color detail by ID
     * GET /api/colors/{id}
     */
    @GetMapping("/colors/{id}")
    @Operation(summary = "Get color detail", description = "Lấy chi tiết một màu sắc")
    public ResponseEntity<ApiResponse<ColorDetailResponse>> getColorDetail(
            @Parameter(description = "Color ID") @PathVariable Long id) {
        log.info("User API: Getting color detail for ID: {}", id);
        ApiResponse<ColorDetailResponse> response = colorService.getColorDetail(id);
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN APIS ====================

    /**
     * Get all colors for admin with pagination and search
     * GET /api/admin/colors
     */
    @GetMapping("/admin/colors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all colors for admin", description = "Lấy tất cả colors cho admin với phân trang và tìm kiếm")
    public ResponseEntity<PagedApiResponse<ColorResponse>> getAllColorsAdmin(
            @Parameter(description = "Page number (default: 1)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Items per page (default: 20)")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field (colorName, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order (asc, desc)")
            @RequestParam(defaultValue = "asc") String sortOrder) {

        log.info("Admin API: Getting all colors - page: {}, limit: {}, search: {}", page, limit, search);
        PagedApiResponse<ColorResponse> response =
                colorService.getAllColorsAdmin(page, limit, search, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    /**
     * Create new color
     * POST /api/admin/colors
     */
    @PostMapping("/admin/colors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new color", description = "Tạo màu sắc mới")
    public ResponseEntity<ApiResponse<ColorDetailResponse>> createColor(
            @Valid @RequestBody CreateColorRequest request) {
        log.info("Admin API: Creating new color: {}", request.getColorName());
        ApiResponse<ColorDetailResponse> response = colorService.createColor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update color
     * PUT /api/admin/colors/{id}
     */
    @PutMapping("/admin/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update color", description = "Cập nhật màu sắc")
    public ResponseEntity<ApiResponse<ColorDetailResponse>> updateColor(
            @Parameter(description = "Color ID") @PathVariable Long id,
            @Valid @RequestBody UpdateColorAdminRequest request) {
        log.info("Admin API: Updating color ID: {}", id);
        ApiResponse<ColorDetailResponse> response = colorService.updateColor(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete color
     * DELETE /api/admin/colors/{id}
     */
    @DeleteMapping("/admin/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete color", description = "Xóa màu sắc")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteColor(
            @Parameter(description = "Color ID") @PathVariable Long id) {
        log.info("Admin API: Deleting color ID: {}", id);
        ApiResponse<Map<String, Object>> response = colorService.deleteColor(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get color usage information
     * GET /api/admin/colors/{id}/usage
     */
    @GetMapping("/admin/colors/{id}/usage")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get color usage", description = "Xem màu sắc đang được sử dụng bởi products nào")
    public ResponseEntity<ApiResponse<ColorUsageResponse>> getColorUsage(
            @Parameter(description = "Color ID") @PathVariable Long id) {
        log.info("Admin API: Getting color usage for ID: {}", id);
        ApiResponse<ColorUsageResponse> response = colorService.getColorUsage(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get color statistics
     * GET /api/admin/colors/statistics
     */
    @GetMapping("/admin/colors/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get color statistics", description = "Thống kê tổng quan về colors")
    public ResponseEntity<ApiResponse<ColorStatisticsResponse>> getColorStatistics() {
        log.info("Admin API: Getting color statistics");
        ApiResponse<ColorStatisticsResponse> response = colorService.getColorStatistics();
        return ResponseEntity.ok(response);
    }
}
package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.category.CreateCategoryRequest;
import fit.se.be_phone_store.dto.request.category.UpdateCategoryRequest;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.dto.response.category.CategoryOverviewResponse;
import fit.se.be_phone_store.dto.response.category.CategoryResponse;
import fit.se.be_phone_store.dto.response.category.CategoryStatisticsResponse;
import fit.se.be_phone_store.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CategoryController - Handles both user and admin category endpoints
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    // ========================================
    // USER APIs
    // ========================================

    /**
     * Get Categories List (User)
     * GET /api/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        log.info("Getting categories list for user");
        ApiResponse<List<CategoryResponse>> response = categoryService.getAllCategories();
        response.setMessage("Lấy danh sách danh mục thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get Category Detail (User)
     * GET /api/categories/{id}
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryDetail(@PathVariable Long id) {
        log.info("Getting category detail for ID: {}", id);
        ApiResponse<CategoryResponse> response = categoryService.getCategoryDetail(id);
        response.setMessage("Lấy chi tiết danh mục thành công");
        return ResponseEntity.ok(response);
    }

    // ========================================
    // ADMIN APIs
    // ========================================

    /**
     * Get All Categories (Admin)
     * GET /api/admin/categories
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesAdmin(
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "asc") String sort_order) {
        log.info("Getting all categories for admin - sortBy: {}, sortOrder: {}", sort_by, sort_order);
        ApiResponse<List<CategoryResponse>> response = categoryService.getAllCategoriesAdmin(sort_by, sort_order);
        response.setMessage("Lấy danh sách danh mục thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Create Category (Admin)
     * POST /api/admin/categories
     */
    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        ApiResponse<CategoryResponse> response = categoryService.createCategory(request);
        response.setMessage("Tạo danh mục thành công");
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Update Category (Admin)
     * PUT /api/admin/categories/{id}
     */
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("Updating category ID: {} with name: {}", id, request.getName());
        ApiResponse<CategoryResponse> response = categoryService.updateCategory(id, request);
        response.setMessage("Cập nhật danh mục thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Category (Admin)
     * DELETE /api/admin/categories/{id}
     */
    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category ID: {}", id);
        ApiResponse<Map<String, Object>> response = categoryService.deleteCategory(id);
        response.setMessage("Xóa danh mục thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get Category Statistics (Admin)
     * GET /api/admin/categories/{id}/statistics
     */
    @GetMapping("/admin/categories/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryStatisticsResponse>> getCategoryStatistics(@PathVariable Long id) {
        log.info("Getting statistics for category ID: {}", id);
        ApiResponse<CategoryStatisticsResponse> response = categoryService.getCategoryStatistics(id);
        response.setMessage("Lấy thống kê danh mục thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get Categories Overview (Admin)
     * GET /api/admin/categories/overview
     */
    @GetMapping("/admin/categories/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryOverviewResponse>> getCategoriesOverview() {
        log.info("Getting categories overview");
        ApiResponse<CategoryOverviewResponse> response = categoryService.getCategoriesOverview();
        response.setMessage("Lấy tổng quan danh mục thành công");
        return ResponseEntity.ok(response);
    }
}
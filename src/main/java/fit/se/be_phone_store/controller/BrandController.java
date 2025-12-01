package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.brand.CreateBrandRequest;
import fit.se.be_phone_store.dto.request.brand.UpdateBrandRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.brand.BrandResponse;
import fit.se.be_phone_store.service.BrandService;
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
 * BrandController - Handles both user and admin brand endpoints
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class BrandController {

    private final BrandService brandService;

    // ========================================
    // USER APIs
    // ========================================

    /**
     * Get Brands List (User)
     * GET /api/brands
     */
    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getBrands() {
        log.info("Getting brands list for user");
        ApiResponse<List<BrandResponse>> response = brandService.getAllBrands();
        return ResponseEntity.ok(response);
    }

    /**
     * Get Brand Detail (User)
     * GET /api/brands/{id}
     */
    @GetMapping("/brands/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandDetail(@PathVariable Long id) {
        log.info("Getting brand detail for ID: {}", id);
        ApiResponse<BrandResponse> response = brandService.getBrandDetail(id);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // ADMIN APIs
    // ========================================

    /**
     * Get All Brands (Admin)
     * GET /api/admin/brands
     */
    @GetMapping("/admin/brands")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedApiResponse<BrandResponse>> getAllBrandsAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "name") String sort_by,
            @RequestParam(defaultValue = "asc") String sort_order) {

        log.info("Getting all brands for admin - page: {}, limit: {}, search: {}, sortBy: {}, sortOrder: {}",
                page, limit, search, sort_by, sort_order);

        PagedApiResponse<BrandResponse> response = brandService.getAllBrandsAdmin(
                page, limit, search, sort_by, sort_order);

        return ResponseEntity.ok(response);
    }

    /**
     * Create Brand (Admin)
     * POST /api/admin/brands
     */
    @PostMapping("/admin/brands")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody CreateBrandRequest request) {
        log.info("Creating new brand: {}", request.getName());
        ApiResponse<BrandResponse> response = brandService.createBrand(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Update Brand (Admin)
     * PUT /api/admin/brands/{id}
     */
    @PutMapping("/admin/brands/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request) {
        log.info("Updating brand ID: {} with name: {}", id, request.getName());
        ApiResponse<BrandResponse> response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Brand (Admin)
     * DELETE /api/admin/brands/{id}
     */
    @DeleteMapping("/admin/brands/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteBrand(@PathVariable Long id) {
        log.info("Deleting brand ID: {}", id);
        ApiResponse<Map<String, Object>> response = brandService.deleteBrand(id);
        return ResponseEntity.ok(response);
    }
}
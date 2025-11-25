package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.product.ProductFilterRequest;
import fit.se.be_phone_store.dto.request.product.*;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.product.ProductResponse;
import fit.se.be_phone_store.dto.response.product.StockUpdateResponse;
import fit.se.be_phone_store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ProductController - Handles both user and admin product endpoints
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductController {

    private final ProductService productService;

    // ========================================
    // USER APIs
    // ========================================

    /**
     * Get Products List (With Search & Filter)
     * GET /api/products
     */
    @GetMapping("/api/products")
    public ResponseEntity<PagedApiResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long brand_id,
            @RequestParam(required = false) Long color_id,
            @RequestParam(required = false) BigDecimal min_price,
            @RequestParam(required = false) BigDecimal max_price,
            @RequestParam(required = false) Boolean in_stock,
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order) {

        log.info("Getting products - page: {}, limit: {}, search: {}", page, limit, search);

        ProductFilterRequest filters = new ProductFilterRequest();
        filters.setPage(page);
        filters.setLimit(limit);
        filters.setSearch(search);
        filters.setCategory(category);
        filters.setBrandId(brand_id);
        filters.setColorId(color_id);
        filters.setMinPrice(min_price);
        filters.setMaxPrice(max_price);
        filters.setInStock(in_stock);
        filters.setSortBy(sort_by);
        filters.setSortOrder(sort_order);

        PagedApiResponse<ProductResponse> response = productService.getProducts(filters);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Product Detail
     * GET /api/products/{slug}
     */
    @GetMapping("/api/products/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetail(@PathVariable String slug) {
        log.info("Getting product detail for slug: {}", slug);
        ApiResponse<ProductResponse> response = productService.getProductDetail(slug);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // ADMIN APIs
    // ========================================

    /**
     * Create Product
     * POST /api/admin/products
     */
    @PostMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestPart("product") CreateProductRequest request,
            @RequestPart("images") MultipartFile[] images) {

        log.info("Creating new product: {}", request.getName());
        ApiResponse<ProductResponse> response = productService.createProduct(request, images);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Update Product
     * PUT /api/admin/products/{id}
     */
    @PutMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        log.info("Updating product ID: {}", id);
        ApiResponse<ProductResponse> response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Product
     * DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product ID: {}", id);
        ApiResponse<Map<String, Object>> response = productService.deleteProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update Product Stock
     * PATCH /api/admin/products/{id}/stock
     */
    @PatchMapping("/api/admin/products/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockUpdateResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("Updating stock for product ID: {}", id);
        ApiResponse<StockUpdateResponse> response = productService.updateStock(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Add Colors to Product
     * POST /api/admin/products/{id}/colors
     */
    @PostMapping("/api/admin/products/{id}/colors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addColorsToProduct(
            @PathVariable Long id,
            @Valid @RequestBody ManageProductColorsRequest request) {

        log.info("Adding colors to product ID: {}", id);
        ApiResponse<Map<String, Object>> response = productService.addColorsToProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove Colors from Product
     * DELETE /api/admin/products/{id}/colors
     */
    @DeleteMapping("/api/admin/products/{id}/colors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeColorsFromProduct(
            @PathVariable Long id,
            @Valid @RequestBody ManageProductColorsRequest request) {

        log.info("Removing colors from product ID: {}", id);
        ApiResponse<Map<String, Object>> response = productService.removeColorsFromProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Add Images to Product
     * POST /api/admin/products/{id}/images
     */
    @PostMapping("/api/admin/products/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addImagesToProduct(
            @PathVariable Long id,
            @RequestPart("images") MultipartFile[] images,
            @RequestPart(value = "image_alts", required = false) String[] imageAltsArray) {

        log.info("Adding images to product ID: {}", id);

        List<String> imageAlts = imageAltsArray != null ? Arrays.asList(imageAltsArray) : null;
        ApiResponse<Map<String, Object>> response = productService.addImagesToProduct(id, images, imageAlts);
        return ResponseEntity.ok(response);
    }

    /**
     * Update Image
     * PUT /api/admin/products/{product_id}/images/{image_id}
     */
    @PutMapping("/api/admin/products/{product_id}/images/{image_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateImage(
            @PathVariable Long product_id,
            @PathVariable Long image_id,
            @Valid @RequestBody UpdateImageRequest request) {

        log.info("Updating image ID: {} for product ID: {}", image_id, product_id);
        ApiResponse<Map<String, Object>> response = productService.updateImage(product_id, image_id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Image
     * DELETE /api/admin/products/{product_id}/images/{image_id}
     */
    @DeleteMapping("/api/admin/products/{product_id}/images/{image_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteImage(
            @PathVariable Long product_id,
            @PathVariable Long image_id) {

        log.info("Deleting image ID: {} from product ID: {}", image_id, product_id);
        ApiResponse<Map<String, Object>> response = productService.deleteImage(product_id, image_id);
        return ResponseEntity.ok(response);
    }

    /**
     * Add Specifications to Product
     * POST /api/admin/products/{id}/specifications
     */
    @PostMapping("/api/admin/products/{id}/specifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addSpecificationsToProduct(
            @PathVariable Long id,
            @Valid @RequestBody ManageSpecificationsRequest request) {

        log.info("Adding specifications to product ID: {}", id);
        ApiResponse<Map<String, Object>> response = productService.addSpecificationsToProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update Specification
     * PUT /api/admin/products/{product_id}/specifications/{spec_id}
     */
    @PutMapping("/api/admin/products/{product_id}/specifications/{spec_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSpecification(
            @PathVariable Long product_id,
            @PathVariable Long spec_id,
            @Valid @RequestBody UpdateSpecificationRequest request) {

        log.info("Updating specification ID: {} for product ID: {}", spec_id, product_id);
        ApiResponse<Map<String, Object>> response = productService.updateSpecification(product_id, spec_id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Specification
     * DELETE /api/admin/products/{product_id}/specifications/{spec_id}
     */
    @DeleteMapping("/api/admin/products/{product_id}/specifications/{spec_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSpecification(
            @PathVariable Long product_id,
            @PathVariable Long spec_id) {

        log.info("Deleting specification ID: {} from product ID: {}", spec_id, product_id);
        ApiResponse<Map<String, Object>> response = productService.deleteSpecification(product_id, spec_id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get All Products (Admin)
     * GET /api/admin/products
     */
    @GetMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedApiResponse<ProductResponse>> getAllProductsAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long brand_id,
            @RequestParam(required = false) Long color_id,
            @RequestParam(required = false) BigDecimal min_price,
            @RequestParam(required = false) BigDecimal max_price,
            @RequestParam(required = false) Boolean in_stock,
            @RequestParam(required = false) Boolean is_active,
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order) {

        log.info("Getting all products for admin - page: {}, limit: {}", page, limit);

        ProductFilterRequest filters = new ProductFilterRequest();
        filters.setPage(page);
        filters.setLimit(limit);
        filters.setSearch(search);
        filters.setCategory(category);
        filters.setBrandId(brand_id);
        filters.setColorId(color_id);
        filters.setMinPrice(min_price);
        filters.setMaxPrice(max_price);
        filters.setInStock(in_stock);
        filters.setIsActive(is_active);
        filters.setSortBy(sort_by);
        filters.setSortOrder(sort_order);

        PagedApiResponse<ProductResponse> response = productService.getAllProductsAdmin(filters);
        return ResponseEntity.ok(response);
    }
}
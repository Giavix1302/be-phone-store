package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.AdminDeleteReviewRequest;
import fit.se.be_phone_store.dto.request.CreateReviewRequest;
import fit.se.be_phone_store.dto.request.UpdateReviewRequest;
import fit.se.be_phone_store.dto.response.AdminDeleteReviewResponse;
import fit.se.be_phone_store.dto.response.AdminReviewStatisticsResponse;
import fit.se.be_phone_store.dto.response.AdminReviewsResponse;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.CreateReviewResponse;
import fit.se.be_phone_store.dto.response.DeleteReviewResponse;
import fit.se.be_phone_store.dto.response.ProductReviewStatisticsResponse;
import fit.se.be_phone_store.dto.response.ProductReviewsResponse;
import fit.se.be_phone_store.dto.response.ReviewDetailResponse;
import fit.se.be_phone_store.dto.response.UpdateReviewResponse;
import fit.se.be_phone_store.dto.response.UserReviewsResponse;
import fit.se.be_phone_store.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ReviewController - Handles review management endpoints
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Get Product Reviews
     * GET /api/products/{product_id}/reviews
     */
    @GetMapping("/products/{product_id}/reviews")
    public ResponseEntity<ApiResponse<ProductReviewsResponse>> getProductReviews(
            @PathVariable("product_id") Long productId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "sort_by", defaultValue = "created_at") String sortBy,
            @RequestParam(value = "sort_order", defaultValue = "desc") String sortOrder) {
        
        log.info("Getting reviews for product: {}", productId);
        ApiResponse<ProductReviewsResponse> response = reviewService.getProductReviews(
                productId, page, limit, rating, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Product Review Statistics
     * GET /api/products/{product_id}/reviews/statistics
     */
    @GetMapping("/products/{product_id}/reviews/statistics")
    public ResponseEntity<ApiResponse<ProductReviewStatisticsResponse>> getProductReviewStatistics(
            @PathVariable("product_id") Long productId) {
        log.info("Getting review statistics for product: {}", productId);
        ApiResponse<ProductReviewStatisticsResponse> response = reviewService.getProductReviewStatistics(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create Review
     * POST /api/reviews
     */
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<CreateReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("Creating review for product: {}", request.getProductId());
        ApiResponse<CreateReviewResponse> response = reviewService.createReview(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Review Detail
     * GET /api/reviews/{review_id}
     */
    @GetMapping("/reviews/{review_id}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReviewDetail(
            @PathVariable("review_id") Long reviewId) {
        log.info("Getting review detail {}", reviewId);
        ApiResponse<ReviewDetailResponse> response = reviewService.getReviewDetail(reviewId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all reviews (admin)
     * GET /api/admin/reviews
     */
    @GetMapping("/admin/reviews")
    public ResponseEntity<ApiResponse<AdminReviewsResponse>> getAllReviewsForAdmin(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "product_id", required = false) Long productId,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "from_date", required = false) String fromDate,
            @RequestParam(value = "to_date", required = false) String toDate,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort_by", defaultValue = "created_at") String sortBy,
            @RequestParam(value = "sort_order", defaultValue = "desc") String sortOrder) {
        log.info("Admin fetching reviews");
        ApiResponse<AdminReviewsResponse> response = reviewService.getAllReviewsForAdmin(
                page, limit, productId, userId, rating, fromDate, toDate, search, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get review statistics (admin)
     * GET /api/admin/reviews/statistics
     */
    @GetMapping("/admin/reviews/statistics")
    public ResponseEntity<ApiResponse<AdminReviewStatisticsResponse>> getAdminReviewStatistics(
            @RequestParam(value = "period", defaultValue = "month") String period,
            @RequestParam(value = "from_date", required = false) String fromDate,
            @RequestParam(value = "to_date", required = false) String toDate,
            @RequestParam(value = "product_id", required = false) Long productId
    ) {
        log.info("Admin fetching review statistics, period {}", period);
        ApiResponse<AdminReviewStatisticsResponse> response = reviewService.getAdminReviewStatistics(
                period, fromDate, toDate, productId
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Delete review (admin)
     * DELETE /api/admin/reviews/{review_id}
     */
    @DeleteMapping("/admin/reviews/{review_id}")
    public ResponseEntity<ApiResponse<AdminDeleteReviewResponse>> deleteReviewAsAdmin(
            @PathVariable("review_id") Long reviewId,
            @Valid @RequestBody AdminDeleteReviewRequest request) {
        log.info("Admin deleting review {}", reviewId);
        ApiResponse<AdminDeleteReviewResponse> response = reviewService.deleteReviewAsAdmin(reviewId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's reviews
     * GET /api/users/me/reviews
     */
    @GetMapping("/users/me/reviews")
    public ResponseEntity<ApiResponse<UserReviewsResponse>> getCurrentUserReviews(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "sort_by", defaultValue = "created_at") String sortBy,
            @RequestParam(value = "sort_order", defaultValue = "desc") String sortOrder) {
        log.info("Getting reviews for current user");
        ApiResponse<UserReviewsResponse> response = reviewService.getCurrentUserReviews(page, limit, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    /**
     * Update Review
     * PUT /api/reviews/{review_id}
     */
    @PutMapping("/reviews/{review_id}")
    public ResponseEntity<ApiResponse<UpdateReviewResponse>> updateReview(
            @PathVariable("review_id") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        log.info("Updating review {}", reviewId);
        ApiResponse<UpdateReviewResponse> response = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Review
     * DELETE /api/reviews/{review_id}
     */
    @DeleteMapping("/reviews/{review_id}")
    public ResponseEntity<ApiResponse<DeleteReviewResponse>> deleteReview(
            @PathVariable("review_id") Long reviewId) {
        log.info("Deleting review {}", reviewId);
        ApiResponse<DeleteReviewResponse> response = reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(response);
    }
}


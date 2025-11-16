package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.CreateReviewRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.CreateReviewResponse;
import fit.se.be_phone_store.dto.response.ProductReviewsResponse;
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
}


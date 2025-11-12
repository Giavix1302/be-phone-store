package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.*;
import fit.se.be_phone_store.repository.*;
import fit.se.be_phone_store.dto.request.CreateReviewRequest;
import fit.se.be_phone_store.dto.request.UpdateReviewRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.ReviewResponse;
import fit.se.be_phone_store.dto.response.ReviewStatisticsResponse;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReviewService - Handles product review management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;

    /**
     * Get reviews for a product with pagination
     * @param productId Product ID
     * @param pageable Pagination parameters
     * @return Paged API response with reviews
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        log.info("Getting reviews for product: {}", productId);

        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Page<Review> reviewsPage = reviewRepository.findByProductId(productId, pageable);
        Page<ReviewResponse> responsePage = reviewsPage.map(this::mapToReviewResponse);

        return PagedApiResponse.success("Product reviews retrieved successfully", responsePage);
    }

    /**
     * Get review statistics for a product
     * @param productId Product ID
     * @return API response with review statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<ReviewStatisticsResponse> getProductReviewStatistics(Long productId) {
        log.info("Getting review statistics for product: {}", productId);

        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Get review statistics
        Object[] stats = reviewRepository.getReviewStatisticsByProductId(productId);
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countByProductId(productId);

        // Build rating distribution
        List<Object[]> ratingCounts = reviewRepository.countReviewsByRatingForProduct(productId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        ratingCounts.forEach(count -> ratingDistribution.put((Integer) count[0], (Long) count[1]));

        ReviewStatisticsResponse response = ReviewStatisticsResponse.builder()
            .productId(productId)
            .totalReviews(totalReviews.intValue())
            .averageRating(averageRating != null ? averageRating : 0.0)
            .ratingDistribution(ratingDistribution)
            .build();

        return ApiResponse.success("Review statistics retrieved successfully", response);
    }

    /**
     * Create a new review for a product
     * @param productId Product ID
     * @param request Create review request
     * @return API response with review data
     */
    public ApiResponse<Map<String, Object>> createReview(Long productId, CreateReviewRequest request) {
        Long userId = authService.getCurrentUserId();
        User currentUser = authService.getCurrentUser();
        log.info("Creating review for product {} by user {}", productId, userId);

        // Verify product exists and is active
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getIsActive()) {
            throw new BadRequestException("Cannot review inactive product");
        }

        // Check if user has already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        // Validate rating range
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        // Check if user has purchased this product (optional verification)
        boolean isVerifiedPurchase = hasUserPurchasedProduct(userId, productId);

        // Create review
        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        // Set verification status based on purchase history
        // This could be made configurable

        Review savedReview = reviewRepository.save(review);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("reviewId", savedReview.getId());
        responseData.put("rating", savedReview.getRating());
        responseData.put("verified", isVerifiedPurchase);

        log.info("Review created successfully: {}", savedReview.getId());
        return ApiResponse.success("Review created successfully", responseData);
    }

    /**
     * Update a review
     * @param reviewId Review ID
     * @param request Update review request
     * @return API response
     */
    public ApiResponse<Map<String, Object>> updateReview(Long reviewId, UpdateReviewRequest request) {
        Long userId = authService.getCurrentUserId();
        log.info("Updating review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own reviews");
        }

        // Update fields if provided
        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new BadRequestException("Rating must be between 1 and 5");
            }
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("reviewId", updatedReview.getId());
        responseData.put("rating", updatedReview.getRating());

        log.info("Review updated successfully: {}", reviewId);
        return ApiResponse.success("Review updated successfully", responseData);
    }

    /**
     * Delete a review
     * @param reviewId Review ID
     * @return API response
     */
    public ApiResponse<Void> deleteReview(Long reviewId) {
        Long userId = authService.getCurrentUserId();
        log.info("Deleting review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Check if user owns this review or is admin
        if (!review.getUser().getId().equals(userId) && !authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", reviewId);

        return ApiResponse.success("Review deleted successfully");
    }

    /**
     * Get user's reviews with pagination
     * @param pageable Pagination parameters
     * @return Paged API response with user's reviews
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ReviewResponse> getUserReviews(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        log.info("Getting reviews for user: {}", userId);

        List<Review> userReviews = reviewRepository.findByUserId(userId);
        List<ReviewResponse> responseList = userReviews.stream()
            .map(this::mapToReviewResponse)
            .collect(Collectors.toList());

        // Create pagination info for user reviews
        PagedApiResponse.PaginationInfo pagination = PagedApiResponse.PaginationInfo.builder()
            .currentPage(0)
            .pageSize(responseList.size())
            .totalElements(responseList.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        return PagedApiResponse.success("User reviews retrieved successfully", responseList, pagination);
    }

    /**
     * Get reviews by rating for a product
     * @param productId Product ID
     * @param rating Rating value (1-5)
     * @param pageable Pagination parameters
     * @return Paged API response with reviews
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ReviewResponse> getProductReviewsByRating(Long productId, Integer rating, Pageable pageable) {
        log.info("Getting reviews for product {} with rating {}", productId, rating);

        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Validate rating
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        List<Review> reviews = reviewRepository.findByProductIdAndRating(productId, rating);
        List<ReviewResponse> responseList = reviews.stream()
            .map(this::mapToReviewResponse)
            .collect(Collectors.toList());

        // Create pagination info
        PagedApiResponse.PaginationInfo pagination = PagedApiResponse.PaginationInfo.builder()
            .currentPage(0)
            .pageSize(responseList.size())
            .totalElements(responseList.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        return PagedApiResponse.success("Reviews retrieved successfully", responseList, pagination);
    }

    /**
     * Get all reviews (Admin only)
     * @param pageable Pagination parameters
     * @return Paged API response with all reviews
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ReviewResponse> getAllReviews(Pageable pageable) {
        log.info("Getting all reviews");

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Page<Review> reviewsPage = reviewRepository.findAll(pageable);
        Page<ReviewResponse> responsePage = reviewsPage.map(this::mapToReviewResponse);

        return PagedApiResponse.success("All reviews retrieved successfully", responsePage);
    }

    /**
     * Get reviews with comments only
     * @param productId Product ID
     * @param pageable Pagination parameters
     * @return Paged API response with reviews that have comments
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ReviewResponse> getProductReviewsWithComments(Long productId, Pageable pageable) {
        log.info("Getting reviews with comments for product: {}", productId);

        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<Review> reviews = reviewRepository.findReviewsWithCommentsByProductId(productId);
        List<ReviewResponse> responseList = reviews.stream()
            .map(this::mapToReviewResponse)
            .collect(Collectors.toList());

        // Create pagination info
        PagedApiResponse.PaginationInfo pagination = PagedApiResponse.PaginationInfo.builder()
            .currentPage(0)
            .pageSize(responseList.size())
            .totalElements(responseList.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        return PagedApiResponse.success("Reviews with comments retrieved successfully", responseList, pagination);
    }

    /**
     * Check if user can review a product
     * @param productId Product ID
     * @return API response with eligibility status
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> checkReviewEligibility(Long productId) {
        Long userId = authService.getCurrentUserId();
        log.info("Checking review eligibility for product {} by user {}", productId, userId);

        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(userId, productId);
        boolean hasPurchased = hasUserPurchasedProduct(userId, productId);
        boolean canReview = !alreadyReviewed && product.getIsActive();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("canReview", canReview);
        responseData.put("alreadyReviewed", alreadyReviewed);
        responseData.put("hasPurchased", hasPurchased);
        responseData.put("productActive", product.getIsActive());

        String message;
        if (!product.getIsActive()) {
            message = "Product is not available for review";
        } else if (alreadyReviewed) {
            message = "You have already reviewed this product";
        } else {
            message = "You can review this product";
        }

        return ApiResponse.success(message, responseData);
    }

    /**
     * Get review statistics for admin dashboard
     * @return API response with review statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getReviewStatistics() {
        log.info("Getting review statistics");

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalReviews", reviewRepository.count());
        statistics.put("positiveReviews", reviewRepository.findPositiveReviews().size());
        statistics.put("negativeReviews", reviewRepository.findNegativeReviews().size());
        statistics.put("reviewsWithComments", reviewRepository.findReviewsWithComments().size());

        // Most reviewed products
        List<Object[]> mostReviewed = reviewRepository.findProductsByReviewCount();
        statistics.put("mostReviewedProducts", mostReviewed);

        // Products with highest ratings
        List<Object[]> highestRated = reviewRepository.findProductsByAverageRating();
        statistics.put("highestRatedProducts", highestRated);

        // Most active reviewers
        List<Object[]> activeReviewers = reviewRepository.findMostActiveReviewers();
        statistics.put("mostActiveReviewers", activeReviewers);

        return ApiResponse.success("Review statistics retrieved successfully", statistics);
    }

    /**
     * Check if user has purchased a product
     */
    private boolean hasUserPurchasedProduct(Long userId, Long productId) {
        return orderRepository.existsByUserId(userId) && 
               !orderRepository.findByUserId(userId).isEmpty() &&
               orderRepository.findByUserId(userId).stream()
                   .anyMatch(order -> order.getOrderItems().stream()
                       .anyMatch(item -> item.getProduct().getId().equals(productId) && 
                                order.getStatus() == Order.OrderStatus.DELIVERED));
    }

    /**
     * Map Review entity to ReviewResponse DTO
     */
    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
            .id(review.getId())
            .userId(review.getUser().getId())
            .username(review.getUser().getUsername())
            .userFullName(review.getUser().getFullName())
            .productId(review.getProduct().getId())
            .productName(review.getProduct().getName())
            .rating(review.getRating())
            .comment(review.getComment())
            .isPositive(review.isPositive())
            .isNegative(review.isNegative())
            .createdAt(review.getCreatedAt())
            .build();
    }
}

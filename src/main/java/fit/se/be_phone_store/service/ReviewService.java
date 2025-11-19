package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.CreateReviewRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.CreateReviewResponse;
import fit.se.be_phone_store.dto.response.ProductReviewsResponse;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Review;
import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.repository.OrderItemRepository;
import fit.se.be_phone_store.repository.ProductRepository;
import fit.se.be_phone_store.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReviewService - Handles product review management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuthService authService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Get product reviews with filtering, sorting, and pagination
     */
    public ApiResponse<ProductReviewsResponse> getProductReviews(
            Long productId,
            Integer page,
            Integer limit,
            Integer rating,
            String sortBy,
            String sortOrder) {
        
        log.info("Getting reviews for product: {} with filters", productId);

        // Get current user ID if authenticated (outside transaction to avoid rollback issues)
        Long currentUserId = null;
        try {
            currentUserId = authService.getCurrentUserId();
        } catch (Exception e) {
            // User not authenticated, continue with null
            log.debug("User not authenticated for review access");
        }

        // Process reviews in transaction
        return getProductReviewsInTransaction(productId, page, limit, rating, sortBy, sortOrder, currentUserId);
    }

    /**
     * Get product reviews with filtering, sorting, and pagination (transactional)
     */
    @Transactional(readOnly = true)
    private ApiResponse<ProductReviewsResponse> getProductReviewsInTransaction(
            Long productId,
            Integer page,
            Integer limit,
            Integer rating,
            String sortBy,
            String sortOrder,
            Long currentUserId) {

        // Verify product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy"));

        // Build pagination
        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;
        
        // Build sorting
        String sortField = mapSortField(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        // Get reviews with filtering and pagination
        Page<Review> reviewPage;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviewPage = reviewRepository.findByProductIdAndRating(productId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findByProductId(productId, pageable);
        }

        // Get review statistics
        Long totalReviews = reviewRepository.countByProductId(productId);
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(productId);
        List<Object[]> ratingCounts = reviewRepository.countReviewsByRatingForProduct(productId);

        Map<String, Long> ratingBreakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingBreakdown.put(String.valueOf(i), 0L);
        }
        ratingCounts.forEach(count -> ratingBreakdown.put(String.valueOf(count[0]), (Long) count[1]));

        // Build response
        ProductReviewsResponse.ProductInfo productInfo = ProductReviewsResponse.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
            .build();

        ProductReviewsResponse.ReviewsSummary reviewsSummary = ProductReviewsResponse.ReviewsSummary.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .ratingBreakdown(ratingBreakdown)
            .build();

        // Map reviews to response items
        List<ProductReviewsResponse.ReviewItem> reviewItems = new ArrayList<>();
        for (Review review : reviewPage.getContent()) {
            ProductReviewsResponse.ReviewItem item = mapToReviewItem(review, currentUserId);
            reviewItems.add(item);
        }

        ProductReviewsResponse.PaginationInfo paginationInfo = ProductReviewsResponse.PaginationInfo.builder()
                .currentPage(reviewPage.getNumber() + 1)
                .totalPages(reviewPage.getTotalPages())
                .totalItems(reviewPage.getTotalElements())
                .itemsPerPage(reviewPage.getSize())
                .hasNext(reviewPage.hasNext())
                .hasPrev(reviewPage.hasPrevious())
            .build();

        ProductReviewsResponse response = ProductReviewsResponse.builder()
                .product(productInfo)
                .reviewsSummary(reviewsSummary)
                .reviews(reviewItems)
                .pagination(paginationInfo)
            .build();

        return ApiResponse.success("Lấy danh sách đánh giá thành công", response);
    }

    /**
     * Map Review entity to ReviewItem DTO
     */
    private ProductReviewsResponse.ReviewItem mapToReviewItem(Review review, Long currentUserId) {
        Long userId = review.getUser().getId();
        String fullName = review.getUser().getFullName();
        String avatar = review.getUser().getAvatar();
        
        boolean isOwnReview = currentUserId != null && currentUserId.equals(userId);
        
        return ProductReviewsResponse.ReviewItem.builder()
                .id(review.getId())
                .user(ProductReviewsResponse.UserInfo.builder()
                        .id(userId)
                        .fullName(fullName)
                        .avatar(avatar)
                        .build())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(formatDateTime(review.getCreatedAt()))
                .updatedAt(formatDateTime(review.getUpdatedAt()))
                .isVerifiedPurchase(false)
                .isOwnReview(isOwnReview)
                .build();
    }

    /**
     * Map sort field from API to entity field
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return "createdAt";
        }
        return switch (sortBy.toLowerCase()) {
            case "rating" -> "rating";
            case "created_at", "createdAt" -> "createdAt";
            default -> "createdAt";
        };
    }

    /**
     * Create a new review
     */
    @Transactional
    public ApiResponse<CreateReviewResponse> createReview(CreateReviewRequest request) {
        log.info("Creating review for product: {}", request.getProductId());
        
        // Get current user
        User currentUser = authService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Verify product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy"));
        
        // Check if user has purchased product with DELIVERED status
        boolean hasPurchased = orderItemRepository.hasUserPurchasedProduct(userId, request.getProductId());
        if (!hasPurchased) {
            throw new BadRequestException("Bạn chỉ có thể đánh giá sản phẩm đã mua");
        }
        
        // Check if user has already reviewed this product
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(userId, request.getProductId());
        if (alreadyReviewed) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
        }
        
        // Validate comment length (200 words)
        int wordCount = countWords(request.getComment());
        //log.info("Comment word count: {} for comment: {}", wordCount, request.getComment());
        if (wordCount > 200) {
            throw new BadRequestException(
                String.format("Bình luận không được vượt quá 200 từ. Hiện tại: %d từ", wordCount)
            );
        }
        
        // Create review
        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        Review savedReview = reviewRepository.save(review);
        
        // Build response
        CreateReviewResponse.ReviewData reviewData = CreateReviewResponse.ReviewData.builder()
                .id(savedReview.getId())
                .user(CreateReviewResponse.UserInfo.builder()
                        .id(currentUser.getId())
                        .fullName(currentUser.getFullName())
                        .avatar(currentUser.getAvatar())
                        .build())
                .product(CreateReviewResponse.ProductInfo.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .slug(product.getSlug())
                        .build())
                .rating(savedReview.getRating())
                .comment(savedReview.getComment())
                .createdAt(formatDateTime(savedReview.getCreatedAt()))
                .isVerifiedPurchase(true) // Always true for created reviews
                .build();
        
        CreateReviewResponse response = CreateReviewResponse.builder()
                .review(reviewData)
                .build();
        
        return ApiResponse.success("Tạo đánh giá thành công", response);
    }
    
    /**
     * Count words in a string
     * A word is defined as a sequence of characters separated by whitespace
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // Trim and split by one or more whitespace characters
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        // Split by whitespace (spaces, tabs, newlines, etc.)
        String[] words = trimmed.split("\\s+");
        return words.length;
    }

    /**
     * Format LocalDateTime to ISO string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }
}


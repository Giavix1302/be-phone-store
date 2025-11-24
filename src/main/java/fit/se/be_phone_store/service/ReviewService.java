package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.AdminDeleteReviewRequest;
import fit.se.be_phone_store.dto.request.CreateReviewRequest;
import fit.se.be_phone_store.dto.request.UpdateReviewRequest;
import fit.se.be_phone_store.dto.response.AdminDeleteReviewResponse;
import fit.se.be_phone_store.dto.response.AdminReviewsResponse;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.CreateReviewResponse;
import fit.se.be_phone_store.dto.response.DeleteReviewResponse;
import fit.se.be_phone_store.dto.response.ProductReviewsResponse;
import fit.se.be_phone_store.dto.response.ReviewDetailResponse;
import fit.se.be_phone_store.dto.response.UpdateReviewResponse;
import fit.se.be_phone_store.dto.response.UserReviewsResponse;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Review;
import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import fit.se.be_phone_store.repository.OrderItemRepository;
import fit.se.be_phone_store.repository.ProductImageRepository;
import fit.se.be_phone_store.repository.ProductRepository;
import fit.se.be_phone_store.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private final ProductImageRepository productImageRepository;
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
            log.debug("User not authenticated for review access");
        }

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

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy"));

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;
 
        String sortField = mapSortField(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        Page<Review> reviewPage;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviewPage = reviewRepository.findByProductIdAndRating(productId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findByProductId(productId, pageable);
        }


        Long totalReviews = reviewRepository.countByProductId(productId);
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(productId);
        List<Object[]> ratingCounts = reviewRepository.countReviewsByRatingForProduct(productId);

        Map<String, Long> ratingBreakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingBreakdown.put(String.valueOf(i), 0L);
        }
        ratingCounts.forEach(count -> ratingBreakdown.put(String.valueOf(count[0]), (Long) count[1]));


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
     * Map Review entity to user-facing review item
     */
    private UserReviewsResponse.ReviewItem mapToUserReviewItem(Review review) {
        Product product = review.getProduct();
        String primaryImage = getPrimaryImageUrl(product.getId());

        UserReviewsResponse.ProductInfo productInfo = UserReviewsResponse.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .primaryImage(primaryImage)
                .build();

        return UserReviewsResponse.ReviewItem.builder()
                .id(review.getId())
                .product(productInfo)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(formatDateTime(review.getCreatedAt()))
                .updatedAt(formatDateTime(review.getUpdatedAt()))
                .build();
    }

    private AdminReviewsResponse.ReviewItem mapToAdminReviewItem(Review review) {
        User user = review.getUser();
        Product product = review.getProduct();

        LocalDateTime purchaseDate = orderItemRepository.findFirstPurchaseDate(user.getId(), product.getId());

        AdminReviewsResponse.UserInfo userInfo = AdminReviewsResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .build();

        AdminReviewsResponse.ProductInfo productInfo = AdminReviewsResponse.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .build();

        return AdminReviewsResponse.ReviewItem.builder()
                .id(review.getId())
                .user(userInfo)
                .product(productInfo)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(formatDateTime(review.getCreatedAt()))
                .updatedAt(formatDateTime(review.getUpdatedAt()))
                .isVerifiedPurchase(purchaseDate != null)
                .purchaseDate(formatDateTime(purchaseDate))
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

    private String mapAdminSortField(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return "createdAt";
        }
        return switch (sortBy.toLowerCase()) {
            case "rating" -> "rating";
            case "updated_at", "updatedat" -> "updatedAt";
            case "created_at", "createdat" -> "createdAt";
            default -> "createdAt";
        };
    }

    /**
     * Create a new review
     */
    @Transactional
    public ApiResponse<CreateReviewResponse> createReview(CreateReviewRequest request) {
        log.info("Creating review for product: {}", request.getProductId());
        
        User currentUser = authService.getCurrentUser();
        Long userId = currentUser.getId();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy"));
        

        boolean hasPurchased = orderItemRepository.hasUserPurchasedProduct(userId, request.getProductId());
        if (!hasPurchased) {
            throw new BadRequestException("Bạn chỉ có thể đánh giá sản phẩm đã mua");
        }

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
     * Get reviews created by current authenticated user
     */
    @Transactional(readOnly = true)
    public ApiResponse<UserReviewsResponse> getCurrentUserReviews(
            Integer page,
            Integer limit,
            String sortBy,
            String sortOrder) {

        User currentUser = authService.getCurrentUser();
        Long userId = currentUser.getId();

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 10;

        String sortField = mapSortField(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));

        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
        Double averageRating = reviewRepository.calculateAverageRatingByUserId(userId);

        List<UserReviewsResponse.ReviewItem> reviewItems = reviewPage.getContent().stream()
                .map(this::mapToUserReviewItem)
                .toList();

        UserReviewsResponse.PaginationInfo paginationInfo = UserReviewsResponse.PaginationInfo.builder()
                .currentPage(reviewPage.getNumber() + 1)
                .totalPages(reviewPage.getTotalPages())
                .totalItems(reviewPage.getTotalElements())
                .itemsPerPage(reviewPage.getSize())
                .hasNext(reviewPage.hasNext())
                .hasPrev(reviewPage.hasPrevious())
                .build();

        UserReviewsResponse.ReviewsSummary summary = UserReviewsResponse.ReviewsSummary.builder()
                .totalReviews(reviewPage.getTotalElements())
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .build();

        UserReviewsResponse response = UserReviewsResponse.builder()
                .reviews(reviewItems)
                .pagination(paginationInfo)
                .summary(summary)
                .build();

        return ApiResponse.success("Lấy danh sách đánh giá thành công", response);
    }

    /**
     * Get review detail by id
     */
    @Transactional(readOnly = true)
    public ApiResponse<ReviewDetailResponse> getReviewDetail(Long reviewId) {
        Review review = reviewRepository.findByIdWithUserAndProduct(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        User user = review.getUser();
        Product product = review.getProduct();

        boolean isVerified = orderItemRepository.hasUserPurchasedProduct(user.getId(), product.getId());
        LocalDateTime purchaseDate = isVerified
                ? orderItemRepository.findFirstPurchaseDate(user.getId(), product.getId())
                : null;

        ReviewDetailResponse.UserInfo userInfo = ReviewDetailResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .build();

        ReviewDetailResponse.ProductInfo productInfo = ReviewDetailResponse.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .primaryImage(getPrimaryImageUrl(product.getId()))
                .build();

        ReviewDetailResponse response = ReviewDetailResponse.builder()
                .id(review.getId())
                .user(userInfo)
                .product(productInfo)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(formatDateTime(review.getCreatedAt()))
                .updatedAt(formatDateTime(review.getUpdatedAt()))
                .isVerifiedPurchase(isVerified)
                .purchaseDate(formatDateTime(purchaseDate))
                .build();

        return ApiResponse.success("Lấy chi tiết đánh giá thành công", response);
    }

    /**
     * Get all reviews for admin with advanced filtering
     */
    @Transactional(readOnly = true)
    public ApiResponse<AdminReviewsResponse> getAllReviewsForAdmin(
            Integer page,
            Integer limit,
            Long productId,
            Long userId,
            Integer rating,
            String fromDate,
            String toDate,
            String search,
            String sortBy,
            String sortOrder) {

        User currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin()) {
            throw new UnauthorizedException("Chỉ quản trị viên mới có thể xem danh sách đánh giá");
        }

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 20;

        String sortField = mapAdminSortField(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));

        LocalDateTime fromDateTime = parseDate(fromDate, false);
        LocalDateTime toDateTime = parseDate(toDate, true);

        Specification<Review> specification = buildAdminReviewSpecification(
                productId, userId, rating, fromDateTime, toDateTime, search
        );

        Page<Review> reviewPage = reviewRepository.findAll(specification, pageable);

        List<AdminReviewsResponse.ReviewItem> reviewItems = reviewPage.getContent().stream()
                .map(this::mapToAdminReviewItem)
                .toList();

        AdminReviewsResponse.PaginationInfo paginationInfo = AdminReviewsResponse.PaginationInfo.builder()
                .currentPage(reviewPage.getNumber() + 1)
                .totalPages(reviewPage.getTotalPages())
                .totalItems(reviewPage.getTotalElements())
                .itemsPerPage(reviewPage.getSize())
                .hasNext(reviewPage.hasNext())
                .hasPrev(reviewPage.hasPrevious())
                .build();

        AdminReviewsResponse response = AdminReviewsResponse.builder()
                .reviews(reviewItems)
                .pagination(paginationInfo)
                .build();

        return ApiResponse.success("Lấy danh sách đánh giá thành công", response);
    }

    /**
     * Update an existing review
     */
    @Transactional
    public ApiResponse<UpdateReviewResponse> updateReview(Long reviewId, UpdateReviewRequest request) {
        log.info("Updating review {}", reviewId);

        User currentUser = authService.getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        boolean isOwner = currentUser.getId().equals(review.getUser().getId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new BadRequestException("Bạn chỉ có thể chỉnh sửa đánh giá của mình");
        }

        int wordCount = countWords(request.getComment());
        if (wordCount > 200) {
            throw new BadRequestException(
                    String.format("Bình luận không được vượt quá 200 từ. Hiện tại: %d từ", wordCount)
            );
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        Review updatedReview = reviewRepository.save(review);

        UpdateReviewResponse.ReviewData reviewData = UpdateReviewResponse.ReviewData.builder()
                .id(updatedReview.getId())
                .rating(updatedReview.getRating())
                .comment(updatedReview.getComment())
                .updatedAt(formatDateTime(updatedReview.getUpdatedAt()))
                .build();

        UpdateReviewResponse response = UpdateReviewResponse.builder()
                .review(reviewData)
                .build();

        return ApiResponse.success("Cập nhật đánh giá thành công", response);
    }

    /**
     * Delete a review
     */
    @Transactional
    public ApiResponse<DeleteReviewResponse> deleteReview(Long reviewId) {
        log.info("Deleting review {}", reviewId);

        User currentUser = authService.getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        boolean isOwner = currentUser.getId().equals(review.getUser().getId());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new BadRequestException("Bạn chỉ có thể xóa đánh giá của mình");
        }

        reviewRepository.delete(review);

        DeleteReviewResponse response = DeleteReviewResponse.builder()
                .deletedReviewId(reviewId)
                .build();

        return ApiResponse.success("Xóa đánh giá thành công", response);
    }

    /**
     * Delete review as admin with reason
     */
    @Transactional
    public ApiResponse<AdminDeleteReviewResponse> deleteReviewAsAdmin(Long reviewId, AdminDeleteReviewRequest request) {
        log.info("Admin deleting review {} for reason {}", reviewId, request.getReason());

        User currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin()) {
            throw new UnauthorizedException("Chỉ quản trị viên mới có thể xóa đánh giá này");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        reviewRepository.delete(review);

        LocalDateTime deletedAt = LocalDateTime.now();

        AdminDeleteReviewResponse.DeletedByInfo deletedByInfo = AdminDeleteReviewResponse.DeletedByInfo.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .build();

        AdminDeleteReviewResponse response = AdminDeleteReviewResponse.builder()
                .deletedReviewId(reviewId)
                .reason(request.getReason())
                .deletedAt(formatDateTime(deletedAt))
                .deletedBy(deletedByInfo)
                .build();

        return ApiResponse.success("Xóa đánh giá thành công", response);
    }

    private Specification<Review> buildAdminReviewSpecification(
            Long productId,
            Long userId,
            Integer rating,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productId != null) {
                predicates.add(cb.equal(root.get("product").get("id"), productId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (rating != null && rating >= 1 && rating <= 5) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }
            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("comment")), keyword));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDateTime parseDate(String value, boolean endOfDay) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return endOfDay ? date.atTime(LocalTime.MAX) : date.atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Định dạng ngày không hợp lệ: " + value);
        }
    }
    
    /**
     * Count words in a string
     * A word is defined as a sequence of characters separated by whitespace
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
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

    private String getPrimaryImageUrl(Long productId) {
        List<String> imageUrls = productImageRepository.findImageUrlsByProductId(productId);
        return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }
}


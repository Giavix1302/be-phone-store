package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.*;
import fit.se.be_phone_store.repository.*;
import fit.se.be_phone_store.dto.request.*;
import fit.se.be_phone_store.dto.response.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OrderService - Handles order management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final ColorRepository colorRepository;
    private final ReviewRepository reviewRepository;
    private final AuthService authService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Create order from user's cart
     * @param request Create order request
     * @return API response with order data
     */
    public ApiResponse<OrderCreatedResponse> createOrderFromCart(CreateOrderRequest request) {
        Long userId = authService.getCurrentUserId();
        User currentUser = authService.getCurrentUser();
        log.info("Creating order for user: {}", userId);

        // Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Cart not found"));

        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống, không thể tạo đơn hàng");
        }

        // Validate cart items (stock, active products)
        List<OutOfStockResponse.OutOfStockItem> outOfStockItems = validateCartItems(cartItems);
        if (!outOfStockItems.isEmpty()) {
            OutOfStockResponse errorData = OutOfStockResponse.builder()
                .out_of_stock_items(outOfStockItems)
                .build();
            throw new BadRequestException("Một số sản phẩm đã hết hàng", errorData);
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUser(currentUser);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        // Convert payment method string to enum
        Order.PaymentMethod paymentMethod = Order.PaymentMethod.COD;
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isEmpty()) {
            try {
                paymentMethod = Order.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Default to COD if invalid
                paymentMethod = Order.PaymentMethod.COD;
            }
        }
        order.setPaymentMethod(paymentMethod);
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        Order savedOrder = orderRepository.save(order);

        // Create initial tracking event
        OrderTracking initialTracking = new OrderTracking();
        initialTracking.setOrder(savedOrder);
        initialTracking.setStatus(Order.OrderStatus.PENDING);
        initialTracking.setDescription("Đơn hàng đã được tạo");
        initialTracking.setLocation("");
        orderTrackingRepository.save(initialTracking);
        List<OrderItem> createdOrderItems = new java.util.ArrayList<>();

        // Create order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setColor(cartItem.getColor());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            
            OrderItem savedItem = orderItemRepository.save(orderItem);
            createdOrderItems.add(savedItem);

            // Update product stock (check ProductColor stock)
            Product product = cartItem.getProduct();
            // For now, update main product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cartItemRepository.deleteByCart(cart);

        // Build response data
        List<OrderCreatedResponse.OrderItemInfo> itemInfos = createdOrderItems.stream()
                .map(item -> OrderCreatedResponse.OrderItemInfo.builder()
                        .id(item.getId())
                        .product_id(item.getProduct().getId())
                        .product_name(item.getProduct().getName())
                        .product_image(getProductImage(item.getProduct()))
                        .color_name(item.getColor() != null ? item.getColor().getColorName() : "")
                        .quantity(item.getQuantity())
                        .unit_price(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        OrderCreatedResponse.OrderInfo orderInfo = OrderCreatedResponse.OrderInfo.builder()
                .id(savedOrder.getId())
                .order_number(savedOrder.getOrderNumber())
                .user_id(currentUser.getId())
                .total_amount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .payment_method(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
                .shipping_address(savedOrder.getShippingAddress())
                .note(savedOrder.getNotes())
                .items(itemInfos)
                .created_at(savedOrder.getCreatedAt())
                .build();

        OrderCreatedResponse responseData = OrderCreatedResponse.builder()
                .order(orderInfo)
                .build();

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return ApiResponse.success("Tạo đơn hàng thành công", responseData);
    }

    /**
     * Get user orders with pagination and filters
     * @param status Order status filter
     * @param fromDate Start date filter
     * @param toDate End date filter
     * @param pageable Pagination parameters
     * @return API response with orders list
     */
    @Transactional(readOnly = true)
    public ApiResponse<OrderListResponse> getUserOrders(String status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        log.info("Getting orders for user: {}, status: {}", userId, status);

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByUserIdAndStatus(userId, orderStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }
        } else {
            orders = orderRepository.findByUserId(userId);
        }

        // Filter by date range
        if (fromDate != null || toDate != null) {
            LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;
            
            orders = orders.stream()
                .filter(order -> {
                    if (fromDateTime != null && order.getCreatedAt().isBefore(fromDateTime)) {
                        return false;
                    }
                    if (toDateTime != null && order.getCreatedAt().isAfter(toDateTime)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        }

        // Sort and paginate
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())); // Default desc
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), orders.size());
        List<Order> pagedOrders = orders.subList(start, end);

        List<OrderListResponse.OrderItem> orderItems = pagedOrders.stream()
            .map(this::mapToOrderListItem)
            .collect(Collectors.toList());

        OrderListResponse.PaginationInfo pagination = OrderListResponse.PaginationInfo.builder()
            .current_page(pageable.getPageNumber() + 1)
            .total_pages((int) Math.ceil((double) orders.size() / pageable.getPageSize()))
            .total_items((long) orders.size())
            .items_per_page(pageable.getPageSize())
            .has_next(end < orders.size())
            .has_prev(start > 0)
            .build();

        OrderListResponse response = OrderListResponse.builder()
            .orders(orderItems)
            .pagination(pagination)
            .build();

        return ApiResponse.success("Orders retrieved successfully", response);
    }

    /**
     * Get order detail by order number
     * @param orderNumber Order number
     * @return API response with order details
     */
    @Transactional(readOnly = true)
    public ApiResponse<OrderDetailResponseNew> getOrderDetail(String orderNumber) {
        Long userId = authService.getCurrentUserId();
        log.info("Getting order details for order {} by user {}", orderNumber, userId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user can access this order
        if (!order.getUser().getId().equals(userId) && !authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Access denied");
        }

        OrderDetailResponseNew response = mapToOrderDetailResponseNew(order);
        return ApiResponse.success("Order details retrieved successfully", response);
    }

    /**
     * Cancel order by order number
     * @param orderNumber Order number
     * @param request Cancel order request with reason
     * @return API response
     */
    public ApiResponse<Map<String, Object>> cancelOrder(String orderNumber, CancelOrderRequest request) {
        Long userId = authService.getCurrentUserId();
        log.info("Cancelling order {} by user {}", orderNumber, userId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user can cancel this order
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        // Cancel order and restore stock
        order.setStatus(Order.OrderStatus.CANCELLED);
        if (request != null && request.getReason() != null) {
            order.setNotes(order.getNotes() != null ? 
                order.getNotes() + "\nCancel reason: " + request.getReason() : 
                "Cancel reason: " + request.getReason());
        }
        orderRepository.save(order);

        // Restore product stock
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("order_number", orderNumber);
        responseData.put("status", "CANCELLED");
        responseData.put("cancelled_at", LocalDateTime.now());
        if (request != null) {
            responseData.put("cancel_reason", request.getReason());
        }

        log.info("Order {} cancelled successfully", orderNumber);
        return ApiResponse.success("Order cancelled successfully", responseData);
    }

    /**
     * Get all orders (Admin only)
     * @param pageable Pagination parameters
     * @return Paged API response with orders
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Getting all orders");

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Page<Order> ordersPage = orderRepository.findAll(pageable);
        Page<OrderResponse> responsePage = ordersPage.map(this::mapToOrderResponse);

        return PagedApiResponse.success("Orders retrieved successfully", responsePage);
    }

    /**
     * Get orders by status (Admin only)
     * @param status Order status
     * @param pageable Pagination parameters
     * @return Paged API response with orders
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<OrderResponse> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        log.info("Getting orders by status: {}", status);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Page<Order> ordersPage = orderRepository.findByStatus(status, pageable);
        Page<OrderResponse> responsePage = ordersPage.map(this::mapToOrderResponse);

        return PagedApiResponse.success("Orders retrieved successfully", responsePage);
    }

    /**
     * Update order status (Admin only)
     * @param orderId Order ID
     * @param status New status
     * @return API response
     */
    public ApiResponse<Map<String, Object>> updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.info("Updating order {} status to: {}", orderId, status);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        orderRepository.save(order);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orderId", orderId);
        responseData.put("oldStatus", oldStatus.name());
        responseData.put("newStatus", status.name());

        log.info("Order {} status updated from {} to {}", orderId, oldStatus, status);
        return ApiResponse.success("Order status updated successfully", responseData);
    }

    /**
     * Get order statistics (Admin only)
     * @return API response with order statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getOrderStatistics() {
        log.info("Getting order statistics");

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Map<String, Object> statistics = new HashMap<>();
        
        // Basic counts
        statistics.put("totalOrders", orderRepository.count());
        statistics.put("pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        statistics.put("processingOrders", orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        statistics.put("shippedOrders", orderRepository.countByStatus(Order.OrderStatus.SHIPPED));
        statistics.put("deliveredOrders", orderRepository.countByStatus(Order.OrderStatus.DELIVERED));
        statistics.put("cancelledOrders", orderRepository.countByStatus(Order.OrderStatus.CANCELLED));

        // Revenue statistics
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal averageOrderValue = orderRepository.calculateAverageOrderValue();
        statistics.put("averageOrderValue", averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO);

        // Recent revenue (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        BigDecimal recentRevenue = orderRepository.calculateRevenueByDateRange(thirtyDaysAgo, LocalDateTime.now());
        statistics.put("revenueLastMonth", recentRevenue != null ? recentRevenue : BigDecimal.ZERO);

        // Orders by status count
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        statusCounts.forEach(count -> statusMap.put((String) count[0], (Long) count[1]));
        statistics.put("ordersByStatus", statusMap);

        // Monthly revenue data
        List<Object[]> monthlyRevenue = orderRepository.calculateMonthlyRevenue();
        statistics.put("monthlyRevenue", monthlyRevenue);

        return ApiResponse.success("Order statistics retrieved successfully", statistics);
    }

    /**
     * Get orders by date range (Admin only)
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Paged API response with orders
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Getting orders between {} and {}", startDate, endDate);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Page<Order> ordersPage = orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        Page<OrderResponse> responsePage = ordersPage.map(this::mapToOrderResponse);

        return PagedApiResponse.success("Orders retrieved successfully", responsePage);
    }

    /**
     * Get recent orders (Admin only)
     * @param limit Number of orders to retrieve
     * @return API response with recent orders
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderResponse>> getRecentOrders(int limit) {
        log.info("Getting {} recent orders", limit);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        List<Order> recentOrders = orderRepository.findRecentOrders(
            org.springframework.data.domain.PageRequest.of(0, limit));
        
        List<OrderResponse> responseList = recentOrders.stream()
            .map(this::mapToOrderResponse)
            .collect(Collectors.toList());

        return ApiResponse.success("Recent orders retrieved successfully", responseList);
    }

    /**
     * Validate cart items before creating order
     * @return List of out of stock items, empty if all items are available
     */
    private List<OutOfStockResponse.OutOfStockItem> validateCartItems(List<CartItem> cartItems) {
        List<OutOfStockResponse.OutOfStockItem> outOfStockItems = new java.util.ArrayList<>();
        
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            
            // Check if product is active
            if (!product.getIsActive()) {
                outOfStockItems.add(OutOfStockResponse.OutOfStockItem.builder()
                    .product_name(product.getName())
                    .requested_quantity(item.getQuantity())
                    .available_quantity(0)
                    .build());
                continue;
            }
            
            // Check stock availability
            if (product.getStockQuantity() < item.getQuantity()) {
                outOfStockItems.add(OutOfStockResponse.OutOfStockItem.builder()
                    .product_name(product.getName())
                    .requested_quantity(item.getQuantity())
                    .available_quantity(product.getStockQuantity())
                    .build());
            }
        }
        
        return outOfStockItems;
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToOrderResponse(Order order) {
        int itemCount = orderItemRepository.countByOrderId(order.getId()).intValue();
        
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus().name())
            .shippingAddress(order.getShippingAddress())
            .itemCount(itemCount)
            .customerName(order.getUser().getFullName())
            .customerEmail(order.getUser().getEmail())
            .createdAt(order.getCreatedAt())
            .build();
    }

    /**
     * Map Order entity to OrderDetailResponse DTO
     */
    private OrderDetailResponse mapToOrderDetailResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        
        List<OrderDetailResponse.OrderItemResponse> items = orderItems.stream()
            .map(this::mapToOrderItemResponse)
            .collect(Collectors.toList());

        return OrderDetailResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus().name())
            .shippingAddress(order.getShippingAddress())
            .notes(order.getNotes())
            .customerName(order.getUser().getFullName())
            .customerEmail(order.getUser().getEmail())
            .customerPhone(order.getUser().getPhone())
            .items(items)
            .createdAt(order.getCreatedAt())
            .build();
    }

    /**
     * Map OrderItem entity to OrderItemResponse DTO
     */
    private OrderDetailResponse.OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderDetailResponse.OrderItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .build();
    }

    /**
     * Track order by order number
     */
    @Transactional(readOnly = true)
    public ApiResponse<OrderTrackingResponse> trackOrder(String orderNumber) {
        Long userId = authService.getCurrentUserId();
        log.info("Tracking order: {} by user {}", orderNumber, userId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user can access this order
        if (!order.getUser().getId().equals(userId) && !authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Access denied");
        }

        // Get tracking history from database
        List<OrderTracking> trackingHistory = orderTrackingRepository.findByOrderNumber(orderNumber);
        
        OrderTrackingResponse response = buildTrackingResponseFromDatabase(order, trackingHistory);
        return ApiResponse.success("Tracking info retrieved successfully", response);
    }

    /**
     * Submit order review
     * POST /api/orders/{order_number}/review
     */
    public ApiResponse<SubmitOrderReviewResponse> submitOrderReview(String orderNumber, SubmitOrderReviewRequest request) {
        Long userId = authService.getCurrentUserId();
        log.info("Submitting review for order: {} by user {}", orderNumber, userId);

        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tìm thấy"));

        // Check if user owns this order
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền truy cập đơn hàng này");
        }

        // Check if order is delivered
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Chỉ có thể đánh giá đơn hàng đã giao thành công");
        }

        // Load order items explicitly to avoid lazy loading issues
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        log.info("Order {} has {} items", orderNumber, orderItems.size());

        if (orderItems.isEmpty()) {
            throw new BadRequestException("Đơn hàng không có sản phẩm nào");
        }

        // Get product IDs in the order
        List<Long> orderProductIds = orderItems.stream()
            .map(item -> item.getProduct().getId())
            .distinct()
            .collect(Collectors.toList());

        log.info("Products in order: {}", orderProductIds);

        // Get order delivered date (when order status became DELIVERED)
        LocalDateTime orderDeliveredDate = getOrderDeliveredDate(order);
        if (orderDeliveredDate == null) {
            // If no delivered tracking found, use order updated_at as fallback
            orderDeliveredDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        }
        log.info("Order {} delivered at: {}", orderNumber, orderDeliveredDate);

        // Create reviews for each product
        List<SubmitOrderReviewResponse.ReviewedProduct> reviewedProducts = new ArrayList<>();
        LocalDateTime reviewedAt = LocalDateTime.now();

        for (SubmitOrderReviewRequest.ProductReview reviewRequest : request.getReviews()) {
            Long productId = reviewRequest.getProduct_id();
            log.info("Processing review for product ID: {}", productId);

            // Check if product is in order
            if (!orderProductIds.contains(productId)) {
                log.warn("Product {} is not in order {}", productId, orderNumber);
                throw new BadRequestException(String.format("Sản phẩm ID %d không có trong đơn hàng", productId));
            }

            // Get product
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy"));

            boolean reviewedInThisOrder = reviewRepository.existsByUserIdAndProductIdAndCreatedAtAfter(
                userId, productId, orderDeliveredDate);
            
            if (reviewedInThisOrder) {
                // Product already reviewed in this order - get existing review
                Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(userId, productId);
                
                if (existingReview.isPresent()) {
                    Review review = existingReview.get();
                    log.info("Product {} already reviewed in order {} by user {}, review ID: {}", 
                        productId, orderNumber, userId, review.getId());
                    
                    // Build response for already reviewed product
                    SubmitOrderReviewResponse.ReviewedProduct reviewedProduct = SubmitOrderReviewResponse.ReviewedProduct.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .rating(review.getRating())
                        .reviewId(review.getId())
                        .status("already_reviewed")
                        .message("Sản phẩm này đã được đánh giá trong đơn hàng này")
                        .build();
                    
                    reviewedProducts.add(reviewedProduct);
                    continue; // Skip creating new review
                }
            }

            // Check if review exists from a different order (created before this order was delivered)
            Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(userId, productId);
            if (existingReview.isPresent()) {
                Review oldReview = existingReview.get();
                
             
                if (oldReview.getCreatedAt().isBefore(orderDeliveredDate)) {
                    log.info("Product {} was reviewed in a different order, updating review ID {} for order {}", 
                        productId, oldReview.getId(), orderNumber);
                    
                    // Update existing review
                    oldReview.setRating(reviewRequest.getRating());
                    oldReview.setComment(reviewRequest.getComment());
                    Review updatedReview = reviewRepository.save(oldReview);
                    
                    // Build response for updated review
                    SubmitOrderReviewResponse.ReviewedProduct reviewedProduct = SubmitOrderReviewResponse.ReviewedProduct.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .rating(reviewRequest.getRating())
                        .reviewId(updatedReview.getId())
                        .status("updated")
                        .message("Đã cập nhật đánh giá từ đơn hàng khác")
                        .build();
                    
                    reviewedProducts.add(reviewedProduct);
                    continue;
                }
            }

            // Create new review (first time reviewing this product)
            Review review = new Review();
            review.setUser(order.getUser());
            review.setProduct(product);
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());

            Review savedReview = reviewRepository.save(review);
            log.info("Created review ID {} for product ID {} in order {}", savedReview.getId(), productId, orderNumber);

            // Build reviewed product response
            SubmitOrderReviewResponse.ReviewedProduct reviewedProduct = SubmitOrderReviewResponse.ReviewedProduct.builder()
                .productId(product.getId())
                .productName(product.getName())
                .rating(reviewRequest.getRating())
                .reviewId(savedReview.getId())
                .status("created")
                .message("Đánh giá đã được tạo thành công")
                .build();

            reviewedProducts.add(reviewedProduct);
        }

        if (reviewedProducts.isEmpty()) {
            throw new BadRequestException("Không có sản phẩm nào được xử lý");
        }

        // Count created, updated, and already reviewed
        long createdCount = reviewedProducts.stream()
            .filter(p -> "created".equals(p.getStatus()))
            .count();
        long updatedCount = reviewedProducts.stream()
            .filter(p -> "updated".equals(p.getStatus()))
            .count();
        long alreadyReviewedCount = reviewedProducts.stream()
            .filter(p -> "already_reviewed".equals(p.getStatus()))
            .count();

        // Build appropriate message
        String message;
        List<String> parts = new ArrayList<>();
        if (createdCount > 0) {
            parts.add(String.format("%d đánh giá mới", createdCount));
        }
        if (updatedCount > 0) {
            parts.add(String.format("%d đánh giá được cập nhật", updatedCount));
        }
        if (alreadyReviewedCount > 0) {
            parts.add(String.format("%d sản phẩm đã được đánh giá trong đơn hàng này", alreadyReviewedCount));
        }
        
        if (parts.isEmpty()) {
            message = "Đã xử lý đánh giá";
        } else {
            message = "Đã xử lý: " + String.join(", ", parts);
        }

        SubmitOrderReviewResponse responseData = SubmitOrderReviewResponse.builder()
            .orderNumber(orderNumber)
            .reviewedProducts(reviewedProducts)
            .reviewedAt(reviewedAt.format(ISO_FORMATTER))
            .build();

        return ApiResponse.success(message, responseData);
    }

    /**
     * Get all orders (Admin) with filters
     */
    @Transactional(readOnly = true)
    public ApiResponse<AdminOrderListResponse> getAllOrders(
            String status, Long userId, LocalDate fromDate, LocalDate toDate, 
            String search, Pageable pageable) {
        log.info("Getting all orders (Admin) with filters - status: {}, userId: {}, fromDate: {}, toDate: {}, search: {}", 
            status, userId, fromDate, toDate, search);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        // Parse status enum
        Order.OrderStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Order.OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }
        }

        // Convert LocalDate to LocalDateTime for query
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        // Query with filters
        Page<Order> ordersPage = orderRepository.findOrdersWithFilters(
            statusEnum, userId, fromDateTime, toDateTime, search, pageable);
        
        List<AdminOrderListResponse.AdminOrderItem> orderItems = ordersPage.getContent().stream()
            .map(this::mapToAdminOrderItem)
            .collect(Collectors.toList());

        AdminOrderListResponse.PaginationInfo pagination = AdminOrderListResponse.PaginationInfo.builder()
            .current_page(pageable.getPageNumber() + 1)
            .total_pages(ordersPage.getTotalPages())
            .total_items(ordersPage.getTotalElements())
            .items_per_page(pageable.getPageSize())
            .has_next(ordersPage.hasNext())
            .has_prev(ordersPage.hasPrevious())
            .build();

        // Build summary
        AdminOrderListResponse.SummaryInfo summary = buildSummaryInfo();

        AdminOrderListResponse response = AdminOrderListResponse.builder()
            .orders(orderItems)
            .pagination(pagination)
            .summary(summary)
            .build();

        return ApiResponse.success("Orders retrieved successfully", response);
    }

    /**
     * Update order status (Admin)
     */
    public ApiResponse<Map<String, Object>> updateOrderStatus(String orderNumber, UpdateOrderStatusRequest request) {
        log.info("Updating order {} status", orderNumber);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Order.OrderStatus oldStatus = order.getStatus();
        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(request.getStatus().toUpperCase());
            
            // Validate status transition
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                throw new BadRequestException("Invalid status transition from " + oldStatus + " to " + newStatus);
            }

            order.setStatus(newStatus);
            
            if (request.getNote() != null) {
                order.setNotes(order.getNotes() != null ? 
                    order.getNotes() + "\n" + request.getNote() : request.getNote());
            }
            orderRepository.save(order);

            // Create tracking event with tracking info
            OrderTracking tracking = new OrderTracking();
            tracking.setOrder(order);
            tracking.setStatus(newStatus);
            tracking.setDescription(getStatusDescription(newStatus));
            tracking.setLocation(request.getLocation() != null ? request.getLocation() : "");
            tracking.setNote(request.getNote());
            
            // Store tracking info in tracking event
            if (request.getTracking_number() != null && !request.getTracking_number().isEmpty()) {
                tracking.setTrackingNumber(request.getTracking_number());
            }
            if (request.getShipping_partner() != null && !request.getShipping_partner().isEmpty()) {
                tracking.setShippingPartner(request.getShipping_partner());
            }
            if (request.getEstimated_delivery() != null) {
                tracking.setEstimatedDelivery(request.getEstimated_delivery());
            }
            
            orderTrackingRepository.save(tracking);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("order_number", orderNumber);
            responseData.put("old_status", oldStatus.name());
            responseData.put("new_status", newStatus.name());
            responseData.put("updated_at", LocalDateTime.now());
            responseData.put("updated_by", Map.of(
                "id", authService.getCurrentUserId(),
                "full_name", authService.getCurrentUser().getFullName()
            ));

            return ApiResponse.success("Order status updated successfully", responseData);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }
    }

    /**
     * Get order statistics (Admin)
     */
    @Transactional(readOnly = true)
    public ApiResponse<OrderStatisticsResponse> getOrderStatistics(
            String period, LocalDate fromDate, LocalDate toDate) {
        log.info("Getting order statistics (Admin)");

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        // Calculate date range based on period
        if (fromDate == null || toDate == null) {
            LocalDate now = LocalDate.now();
            switch (period.toLowerCase()) {
                case "day":
                    fromDate = now;
                    toDate = now;
                    break;
                case "week":
                    fromDate = now.minusWeeks(1);
                    toDate = now;
                    break;
                case "month":
                    fromDate = now.minusMonths(1);
                    toDate = now;
                    break;
                case "year":
                    fromDate = now.minusYears(1);
                    toDate = now;
                    break;
                default:
                    fromDate = now.minusMonths(1);
                    toDate = now;
            }
        }

        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

        // Get orders in date range
        List<Order> orders = orderRepository.findByCreatedAtBetween(fromDateTime, toDateTime);
        List<Order> deliveredOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());

        // Build statistics
        OrderStatisticsResponse.OverviewInfo overview = OrderStatisticsResponse.OverviewInfo.builder()
            .total_orders(orders.size())
            .total_revenue(deliveredOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .average_order_value(deliveredOrders.isEmpty() ? BigDecimal.ZERO :
                deliveredOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(deliveredOrders.size()), 2, java.math.RoundingMode.HALF_UP))
            .completion_rate(orders.isEmpty() ? 0.0 :
                (double) deliveredOrders.size() / orders.size() * 100)
            .build();

        // Status breakdown
        Map<String, Integer> statusBreakdown = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            statusBreakdown.put(status.name(), 
                (int) orders.stream().filter(o -> o.getStatus() == status).count());
        }

        // Daily stats
        List<Object[]> dailyStatsData = orderRepository.getDailyOrderStatisticsByDateRange(fromDateTime, toDateTime);
        List<OrderStatisticsResponse.DailyStat> dailyStats = dailyStatsData.stream()
            .map(data -> {
                // Handle different date types from database
                LocalDate date;
                if (data[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) data[0]).toLocalDate();
                } else if (data[0] instanceof java.time.LocalDate) {
                    date = (LocalDate) data[0];
                } else if (data[0] instanceof java.util.Date) {
                    date = ((java.util.Date) data[0]).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                } else {
                    // Try to parse as string
                    date = LocalDate.parse(data[0].toString());
                }
                
                Long ordersCount = ((Number) data[1]).longValue();
                BigDecimal revenue = data[2] != null ? 
                    BigDecimal.valueOf(((Number) data[2]).doubleValue()) : BigDecimal.ZERO;
                
                return OrderStatisticsResponse.DailyStat.builder()
                    .date(date.toString())
                    .orders_count(ordersCount.intValue())
                    .revenue(revenue)
                    .build();
            })
            .collect(Collectors.toList());

        // Top products (limit to top 10)
        List<Object[]> topProductsData = orderItemRepository.findTopProductsByRevenueInDateRange(
            fromDateTime, toDateTime, 10);
        List<OrderStatisticsResponse.TopProduct> topProducts = topProductsData.stream()
            .map(data -> {
                Long productId = ((Number) data[0]).longValue();
                String productName = (String) data[1];
                Long quantitySold = ((Number) data[2]).longValue();
                BigDecimal revenue = data[3] != null ? 
                    BigDecimal.valueOf(((Number) data[3]).doubleValue()) : BigDecimal.ZERO;
                
                return OrderStatisticsResponse.TopProduct.builder()
                    .product_id(productId)
                    .product_name(productName)
                    .quantity_sold(quantitySold.intValue())
                    .revenue(revenue)
                    .build();
            })
            .collect(Collectors.toList());

        OrderStatisticsResponse response = OrderStatisticsResponse.builder()
            .period(period)
            .from_date(fromDate)
            .to_date(toDate)
            .overview(overview)
            .status_breakdown(statusBreakdown)
            .daily_stats(dailyStats)
            .top_products(topProducts)
            .build();

        return ApiResponse.success("Order statistics retrieved successfully", response);
    }

    // Helper methods
    private boolean isValidStatusTransition(Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        // PENDING -> PROCESSING, SHIPPED, DELIVERED, CANCELLED
        // PROCESSING -> SHIPPED, DELIVERED
        // SHIPPED -> DELIVERED
        // DELIVERED -> (no transitions)
        // CANCELLED -> (no transitions)
        
        if (oldStatus == Order.OrderStatus.PENDING) {
            return newStatus == Order.OrderStatus.PROCESSING || 
                   newStatus == Order.OrderStatus.SHIPPED || 
                   newStatus == Order.OrderStatus.DELIVERED || 
                   newStatus == Order.OrderStatus.CANCELLED;
        }
        if (oldStatus == Order.OrderStatus.PROCESSING) {
            return newStatus == Order.OrderStatus.SHIPPED || 
                   newStatus == Order.OrderStatus.DELIVERED;
        }
        if (oldStatus == Order.OrderStatus.SHIPPED) {
            return newStatus == Order.OrderStatus.DELIVERED;
        }
        return false;
    }

    /**
     * Get the date when order was delivered
     */
    private LocalDateTime getOrderDeliveredDate(Order order) {
        List<OrderTracking> trackingHistory = orderTrackingRepository.findByOrder(order);
        
        // Find the first tracking event with DELIVERED status
        return trackingHistory.stream()
            .filter(tracking -> tracking.getStatus() == Order.OrderStatus.DELIVERED)
            .min(java.util.Comparator.comparing(OrderTracking::getCreatedAt))
            .map(OrderTracking::getCreatedAt)
            .orElse(null);
    }

    private OrderTrackingResponse buildTrackingResponseFromDatabase(Order order, List<OrderTracking> trackingHistory) {
        List<OrderTrackingResponse.TrackingEvent> events = new java.util.ArrayList<>();
        
        // Get latest tracking info (from most recent tracking event that has tracking info)
        String latestTrackingNumber = "";
        String latestShippingPartner = "";
        LocalDateTime latestEstimatedDelivery = null;
        
        // Convert tracking history to events and find latest tracking info
        for (int i = trackingHistory.size() - 1; i >= 0; i--) {
            OrderTracking tracking = trackingHistory.get(i);
            
            // Get latest tracking info from tracking events
            if (latestTrackingNumber.isEmpty() && tracking.getTrackingNumber() != null && !tracking.getTrackingNumber().isEmpty()) {
                latestTrackingNumber = tracking.getTrackingNumber();
            }
            if (latestShippingPartner.isEmpty() && tracking.getShippingPartner() != null && !tracking.getShippingPartner().isEmpty()) {
                latestShippingPartner = tracking.getShippingPartner();
            }
            if (latestEstimatedDelivery == null && tracking.getEstimatedDelivery() != null) {
                latestEstimatedDelivery = tracking.getEstimatedDelivery();
            }
            
            events.add(OrderTrackingResponse.TrackingEvent.builder()
                .status(tracking.getStatus().name())
                .description(tracking.getDescription() != null ? tracking.getDescription() : getStatusDescription(tracking.getStatus()))
                .location(tracking.getLocation() != null ? tracking.getLocation() : "")
                .timestamp(tracking.getCreatedAt())
                .build());
        }

        return OrderTrackingResponse.builder()
            .order_number(order.getOrderNumber())
            .current_status(order.getStatus().name())
            .tracking_number(latestTrackingNumber)
            .shipping_partner(latestShippingPartner)
            .estimated_delivery(latestEstimatedDelivery)
            .tracking_events(events)
            .build();
    }

    private String getStatusDescription(Order.OrderStatus status) {
        switch (status) {
            case PENDING: return "Đơn hàng đã được tạo";
            case PROCESSING: return "Đơn hàng đang được chuẩn bị";
            case SHIPPED: return "Đơn hàng đã được giao cho đối tác vận chuyển";
            case DELIVERED: return "Đơn hàng đã giao thành công";
            case CANCELLED: return "Đơn hàng đã bị hủy";
            default: return "";
        }
    }

    private OrderListResponse.OrderItem mapToOrderListItem(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        
        List<OrderListResponse.ItemPreview> preview = items.stream()
            .limit(3)
            .map(item -> OrderListResponse.ItemPreview.builder()
                .product_name(item.getProduct().getName())
                .product_image(getProductImage(item.getProduct()))
                .quantity(item.getQuantity())
                .build())
            .collect(Collectors.toList());

        return OrderListResponse.OrderItem.builder()
            .id(order.getId())
            .order_number(order.getOrderNumber())
            .total_amount(order.getTotalAmount())
            .status(order.getStatus().name())
            .payment_method(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
            .items_count(items.size())
            .items_preview(preview)
            .created_at(order.getCreatedAt())
            .delivered_at(order.getStatus() == Order.OrderStatus.DELIVERED ? order.getCreatedAt() : null)
            .can_cancel(order.canBeCancelled())
            .can_review(order.getStatus() == Order.OrderStatus.DELIVERED)
            .build();
    }

    private OrderDetailResponseNew mapToOrderDetailResponseNew(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        
        List<OrderDetailResponseNew.OrderItemDetail> orderItems = items.stream()
            .map(item -> OrderDetailResponseNew.OrderItemDetail.builder()
                .id(item.getId())
                .product(OrderDetailResponseNew.ProductInfo.builder()
                    .id(item.getProduct().getId())
                    .name(item.getProduct().getName())
                    .slug(item.getProduct().getSlug())
                    .image(getProductImage(item.getProduct()))
                    .build())
                .color_name(item.getColor() != null ? item.getColor().getColorName() : "")
                .quantity(item.getQuantity())
                .unit_price(item.getUnitPrice())
                .line_total(item.getTotalPrice())
                .build())
            .collect(Collectors.toList());

        // Build status history from tracking
        List<OrderDetailResponseNew.StatusHistory> statusHistory = buildStatusHistoryFromTracking(order);

        return OrderDetailResponseNew.builder()
            .id(order.getId())
            .order_number(order.getOrderNumber())
            .user(OrderDetailResponseNew.UserInfo.builder()
                .id(order.getUser().getId())
                .full_name(order.getUser().getFullName())
                .email(order.getUser().getEmail())
                .phone(order.getUser().getPhone())
                .build())
            .total_amount(order.getTotalAmount())
            .status(order.getStatus().name())
            .payment_method(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
            .shipping_address(order.getShippingAddress())
            .note(order.getNotes())
            .items(orderItems)
            .status_history(buildStatusHistoryFromTracking(order))
            .tracking_info(buildTrackingInfoFromLatestTracking(order))
            .created_at(order.getCreatedAt())
            .updated_at(order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt())
            .can_cancel(order.canBeCancelled())
            .can_review(order.getStatus() == Order.OrderStatus.DELIVERED)
            .build();
    }

    private AdminOrderListResponse.AdminOrderItem mapToAdminOrderItem(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        
        return AdminOrderListResponse.AdminOrderItem.builder()
            .id(order.getId())
            .order_number(order.getOrderNumber())
            .user(AdminOrderListResponse.UserInfo.builder()
                .id(order.getUser().getId())
                .full_name(order.getUser().getFullName())
                .email(order.getUser().getEmail())
                .phone(order.getUser().getPhone())
                .build())
            .total_amount(order.getTotalAmount())
            .status(order.getStatus().name())
            .payment_method(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
            .items_count(items.size())
            .shipping_address(order.getShippingAddress())
            .created_at(order.getCreatedAt() != null ? order.getCreatedAt().format(ISO_FORMATTER) : null)
            .build();
    }

    private AdminOrderListResponse.SummaryInfo buildSummaryInfo() {
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long processingOrders = orderRepository.countByStatus(Order.OrderStatus.PROCESSING);
        long shippedOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();

        return AdminOrderListResponse.SummaryInfo.builder()
            .total_orders((int) totalOrders)
            .pending_orders((int) pendingOrders)
            .processing_orders((int) processingOrders)
            .shipped_orders((int) shippedOrders)
            .delivered_orders((int) deliveredOrders)
            .cancelled_orders((int) cancelledOrders)
            .total_revenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
            .build();
    }

    private String getProductImage(Product product) {
        // Return primary image or first image - simplified
        return ""; // Would get from ProductImage entity
    }

    /**
     * Build status history from tracking events
     */
    private List<OrderDetailResponseNew.StatusHistory> buildStatusHistoryFromTracking(Order order) {
        List<OrderTracking> trackingHistory = orderTrackingRepository.findByOrderId(order.getId());
        
        return trackingHistory.stream()
            .map(tracking -> OrderDetailResponseNew.StatusHistory.builder()
                .status(tracking.getStatus().name())
                .changed_at(tracking.getCreatedAt())
                .note(tracking.getNote() != null ? tracking.getNote() : tracking.getDescription())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Build tracking info from latest tracking event
     */
    private OrderDetailResponseNew.TrackingInfo buildTrackingInfoFromLatestTracking(Order order) {
        List<OrderTracking> trackingHistory = orderTrackingRepository.findByOrderId(order.getId());
        
        // Find latest tracking info from tracking events (most recent that has tracking info)
        String trackingNumber = "";
        String shippingPartner = "";
        LocalDateTime estimatedDelivery = null;
        
        for (int i = trackingHistory.size() - 1; i >= 0; i--) {
            OrderTracking tracking = trackingHistory.get(i);
            
            if (trackingNumber.isEmpty() && tracking.getTrackingNumber() != null && !tracking.getTrackingNumber().isEmpty()) {
                trackingNumber = tracking.getTrackingNumber();
            }
            if (shippingPartner.isEmpty() && tracking.getShippingPartner() != null && !tracking.getShippingPartner().isEmpty()) {
                shippingPartner = tracking.getShippingPartner();
            }
            if (estimatedDelivery == null && tracking.getEstimatedDelivery() != null) {
                estimatedDelivery = tracking.getEstimatedDelivery();
            }
            
            // If we found all info, break early
            if (!trackingNumber.isEmpty() && !shippingPartner.isEmpty() && estimatedDelivery != null) {
                break;
            }
        }
        
        return OrderDetailResponseNew.TrackingInfo.builder()
            .estimated_delivery(estimatedDelivery)
            .shipping_partner(shippingPartner)
            .tracking_number(trackingNumber)
            .build();
    }
}

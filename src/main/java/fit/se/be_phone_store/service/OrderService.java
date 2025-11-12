package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.*;
import fit.se.be_phone_store.repository.*;
import fit.se.be_phone_store.dto.request.CreateOrderRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.OrderResponse;
import fit.se.be_phone_store.dto.response.OrderDetailResponse;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AuthService authService;

    /**
     * Create order from user's cart
     * @param request Create order request
     * @return API response with order data
     */
    public ApiResponse<Map<String, Object>> createOrder(CreateOrderRequest request) {
        Long userId = authService.getCurrentUserId();
        User currentUser = authService.getCurrentUser();
        log.info("Creating order for user: {}", userId);

        // Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Cart not found"));

        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Validate cart items (stock, active products)
        validateCartItems(cartItems);

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
        if (request.getNotes() != null) {
            order.setAdminNotes(request.getNotes());
        }

        Order savedOrder = orderRepository.save(order);

        // Create order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            
            orderItemRepository.save(orderItem);

            // Update product stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cartItemRepository.deleteByCart(cart);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orderId", savedOrder.getId());
        responseData.put("orderNumber", savedOrder.getOrderNumber());
        responseData.put("totalAmount", savedOrder.getTotalAmount());
        responseData.put("status", savedOrder.getStatus().name());

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return ApiResponse.success("Order created successfully", responseData);
    }

    /**
     * Get user orders with pagination
     * @param pageable Pagination parameters
     * @return Paged API response with orders
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<OrderResponse> getUserOrders(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        log.info("Getting orders for user: {}", userId);

        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);
        Page<OrderResponse> responsePage = ordersPage.map(this::mapToOrderResponse);

        return PagedApiResponse.success("Orders retrieved successfully", responsePage);
    }

    /**
     * Get order details by ID
     * @param orderId Order ID
     * @return API response with order details
     */
    @Transactional(readOnly = true)
    public ApiResponse<OrderDetailResponse> getOrderDetails(Long orderId) {
        Long userId = authService.getCurrentUserId();
        log.info("Getting order details for order {} by user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user can access this order
        if (!authService.canAccessResource(order.getUser().getId())) {
            throw new UnauthorizedException("Access denied");
        }

        OrderDetailResponse response = mapToOrderDetailResponse(order);
        return ApiResponse.success("Order details retrieved successfully", response);
    }

    /**
     * Cancel order
     * @param orderId Order ID
     * @return API response
     */
    public ApiResponse<Void> cancelOrder(Long orderId) {
        Long userId = authService.getCurrentUserId();
        log.info("Cancelling order {} by user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user can cancel this order
        if (!authService.canAccessResource(order.getUser().getId())) {
            throw new UnauthorizedException("Access denied");
        }

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new BadRequestException("Cannot cancel order that is already " + order.getStatus().name().toLowerCase());
        }

        // Cancel order and restore stock
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restore product stock
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        log.info("Order {} cancelled successfully", orderId);
        return ApiResponse.success("Order cancelled successfully");
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
     */
    private void validateCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            
            // Check if product is active
            if (!product.getIsActive()) {
                throw new BadRequestException("Product '" + product.getName() + "' is no longer available");
            }
            
            // Check stock availability
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + 
                                            "'. Available: " + product.getStockQuantity());
            }
        }
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
            .notes(order.getAdminNotes())
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
}

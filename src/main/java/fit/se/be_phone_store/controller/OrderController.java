package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.CancelOrderRequest;
import fit.se.be_phone_store.dto.request.CreateOrderRequest;
import fit.se.be_phone_store.dto.request.SubmitOrderReviewRequest;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * OrderController - Handles user order endpoints
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * Create order from cart
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreatedResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order from cart");
        ApiResponse<OrderCreatedResponse> response = orderService.createOrderFromCart(request);
        response.setMessage("Tạo đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get user orders list
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponse>> getUserOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date,
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order) {
        log.info("Getting user orders, page: {}, limit: {}", page, limit);

        Sort sort = buildSort(sort_by, sort_order);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        ApiResponse<OrderListResponse> response = orderService.getUserOrders(
                status, from_date, to_date, pageable);
        response.setMessage("Lấy danh sách đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get order detail
     * GET /api/orders/{order_number}
     */
    @GetMapping("/{order_number}")
    public ResponseEntity<ApiResponse<OrderDetailResponseNew>> getOrderDetail(
            @PathVariable("order_number") String orderNumber) {
        log.info("Getting order detail: {}", orderNumber);
        ApiResponse<OrderDetailResponseNew> response = orderService.getOrderDetail(orderNumber);
        response.setMessage("Lấy chi tiết đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel order
     * POST /api/orders/{order_number}/cancel
     */
    @PostMapping("/{order_number}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelOrder(
            @PathVariable("order_number") String orderNumber,
            @Valid @RequestBody(required = false) CancelOrderRequest request) {
        log.info("Cancelling order: {}", orderNumber);
        ApiResponse<Map<String, Object>> response = orderService.cancelOrder(orderNumber, request);
        response.setMessage("Hủy đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Track order
     * GET /api/orders/{order_number}/tracking
     */
    @GetMapping("/{order_number}/tracking")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(
            @PathVariable("order_number") String orderNumber) {
        log.info("Tracking order: {}", orderNumber);
        ApiResponse<OrderTrackingResponse> response = orderService.trackOrder(orderNumber);
        response.setMessage("Lấy thông tin tracking thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Submit order review
     * POST /api/orders/{order_number}/review
     */
    @PostMapping("/{order_number}/review")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitOrderReview(
            @PathVariable("order_number") String orderNumber,
            @Valid @RequestBody SubmitOrderReviewRequest request) {
        log.info("Submitting review for order: {}", orderNumber);
        ApiResponse<Map<String, Object>> response = orderService.submitOrderReview(orderNumber, request);
        response.setMessage("Đánh giá đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;

        if ("total_amount".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "totalAmount");
        }
        // Default to created_at
        return Sort.by(direction, "createdAt");
    }
}


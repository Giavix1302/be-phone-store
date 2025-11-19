package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.UpdateOrderStatusRequest;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * AdminOrderController - Handles admin order management endpoints
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * Get all orders (Admin)
     * GET /api/admin/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminOrderListResponse>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long user_id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order) {
        log.info("Getting all orders (Admin)");

        Sort sort = buildSort(sort_by, sort_order);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        ApiResponse<AdminOrderListResponse> response = orderService.getAllOrders(
                status, user_id, from_date, to_date, search, pageable);
        response.setMessage("Lấy danh sách đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (Admin)
     * PUT /api/admin/orders/{order_number}/status
     */
    @PutMapping("/{order_number}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOrderStatus(
            @PathVariable("order_number") String orderNumber,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Updating order status: {}", orderNumber);
        ApiResponse<Map<String, Object>> response = orderService.updateOrderStatus(orderNumber, request);
        response.setMessage("Cập nhật trạng thái đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get order statistics (Admin)
     * GET /api/admin/orders/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date) {
        log.info("Getting order statistics (Admin)");
        ApiResponse<OrderStatisticsResponse> response = orderService.getOrderStatistics(
                period, from_date, to_date);
        response.setMessage("Lấy thống kê đơn hàng thành công");
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


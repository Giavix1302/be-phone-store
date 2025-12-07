package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.DashboardOverviewResponse;
import fit.se.be_phone_store.dto.response.RevenueAnalyticsResponse;
import fit.se.be_phone_store.dto.response.OrdersAnalyticsResponse;
import fit.se.be_phone_store.dto.response.ProductsAnalyticsResponse;
import fit.se.be_phone_store.dto.response.ChartsDataResponse;
import fit.se.be_phone_store.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * AdminDashboardController - Handles admin dashboard endpoints
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * Get dashboard overview (Admin)
     * GET /api/admin/dashboard/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getDashboardOverview(
            @RequestParam(defaultValue = "today") String period) {
        log.info("Getting dashboard overview (Admin) - period: {}", period);

        DashboardOverviewResponse responseData = dashboardService.getDashboardOverview(period);
        ApiResponse<DashboardOverviewResponse> response = ApiResponse.success(
                "Lấy tổng quan dashboard thành công", responseData);
        return ResponseEntity.ok(response);
    }

    /**
     * Get revenue analytics (Admin)
     * GET /api/admin/dashboard/revenue
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date) {
        log.info("Getting revenue analytics (Admin) - period: {}, from_date: {}, to_date: {}", 
                period, from_date, to_date);

        RevenueAnalyticsResponse responseData = dashboardService.getRevenueAnalytics(period, from_date, to_date);
        ApiResponse<RevenueAnalyticsResponse> response = ApiResponse.success(
                "Lấy thống kê doanh thu thành công", responseData);
        return ResponseEntity.ok(response);
    }

    /**
     * Get orders analytics (Admin)
     * GET /api/admin/dashboard/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<OrdersAnalyticsResponse>> getOrdersAnalytics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date) {
        log.info("Getting orders analytics (Admin) - period: {}, from_date: {}, to_date: {}", 
                period, from_date, to_date);

        OrdersAnalyticsResponse responseData = dashboardService.getOrdersAnalytics(period, from_date, to_date);
        ApiResponse<OrdersAnalyticsResponse> response = ApiResponse.success(
                "Lấy thống kê đơn hàng thành công", responseData);
        return ResponseEntity.ok(response);
    }

    /**
     * Get products analytics (Admin)
     * GET /api/admin/dashboard/products
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductsAnalyticsResponse>> getProductsAnalytics() {
        log.info("Getting products analytics (Admin)");

        ProductsAnalyticsResponse responseData = dashboardService.getProductsAnalytics();
        ApiResponse<ProductsAnalyticsResponse> response = ApiResponse.success(
                "Lấy thống kê sản phẩm thành công", responseData);
        return ResponseEntity.ok(response);
    }

    /**
     * Get charts data (Admin)
     * GET /api/admin/dashboard/charts
     */
    @GetMapping("/charts")
    public ResponseEntity<ApiResponse<ChartsDataResponse>> getChartsData(
            @RequestParam String chart_type,
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date) {
        log.info("Getting charts data (Admin) - chart_type: {}, period: {}, from_date: {}, to_date: {}", 
                chart_type, period, from_date, to_date);

        ChartsDataResponse responseData = dashboardService.getChartsData(chart_type, period, from_date, to_date);
        ApiResponse<ChartsDataResponse> response = ApiResponse.success(
                "Lấy dữ liệu biểu đồ thành công", responseData);
        return ResponseEntity.ok(response);
    }
}


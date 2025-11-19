package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.dto.request.*;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CartController - Handles shopping cart endpoints
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CartController {

    private final CartService cartService;

    /**
     * Get user cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDetailResponse>> getCart() {
        log.info("Getting cart for current user");
        ApiResponse<CartDetailResponse> response = cartService.getCart();
        
        if (response.getData() != null && response.getData().getItems() != null && 
            response.getData().getItems().isEmpty()) {
            response.setMessage("Giỏ hàng trống");
        } else {
            response.setMessage("Lấy giỏ hàng thành công");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Add item to cart
     * POST /api/cart/items
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addItemToCart(
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Adding item to cart: productId={}, colorId={}, quantity={}",
                request.getProductId(), request.getColorId(), request.getQuantity());
        ApiResponse<Map<String, Object>> response = cartService.addItemToCart(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/{item_id}/quantity
     */
    @PutMapping("/items/{item_id}/quantity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateQuantity(
            @PathVariable("item_id") Long itemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        log.info("Updating quantity for cart item {} to {}", itemId, request.getQuantity());
        ApiResponse<Map<String, Object>> response = cartService.updateQuantity(itemId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update cart item color
     * PUT /api/cart/items/{item_id}/color
     */
    @PutMapping("/items/{item_id}/color")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateColor(
            @PathVariable("item_id") Long itemId,
            @Valid @RequestBody UpdateColorRequest request) {
        log.info("Updating color for cart item {} to colorId {}", itemId, request.getColor_id());
        ApiResponse<Map<String, Object>> response = cartService.updateColor(itemId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove cart item
     * DELETE /api/cart/items/{item_id}
     */
    @DeleteMapping("/items/{item_id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeItem(
            @PathVariable("item_id") Long itemId) {
        log.info("Removing cart item: {}", itemId);
        ApiResponse<Map<String, Object>> response = cartService.removeItem(itemId);
        response.setMessage("Xóa sản phẩm khỏi giỏ hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Clear cart
     * DELETE /api/cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearCart() {
        log.info("Clearing cart for current user");
        ApiResponse<Map<String, Object>> response = cartService.clearCart();
        response.setMessage("Xóa toàn bộ giỏ hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Validate cart
     * POST /api/cart/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CartValidateResponse>> validateCart() {
        log.info("Validating cart for current user");
        ApiResponse<CartValidateResponse> response = cartService.validateCart();
        response.setMessage("Kiểm tra giỏ hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Sync guest cart
     * POST /api/cart/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<CartSyncResponse>> syncCart(
            @Valid @RequestBody SyncCartRequest request) {
        log.info("Syncing guest cart for current user");
        ApiResponse<CartSyncResponse> response = cartService.syncCart(request);
        response.setMessage("Đồng bộ giỏ hàng thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Get cart count
     * GET /api/cart/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<CartCountResponse>> getCartCount() {
        log.info("Getting cart count for current user");
        ApiResponse<CartCountResponse> response = cartService.getCartCount();
        response.setMessage("Lấy số lượng giỏ hàng thành công");
        return ResponseEntity.ok(response);
    }
}


package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.*;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.entity.*;
import fit.se.be_phone_store.repository.*;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.InsufficientStockException;
import fit.se.be_phone_store.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CartService - Handles shopping cart business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductImageRepository productImageRepository;
    private final AuthService authService;

    /**
     * Get user cart
     */
    @Transactional(readOnly = true)
    public ApiResponse<CartDetailResponse> getCart() {
        User currentUser = authService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        CartDetailResponse response = buildCartDetailResponse(cart);
        
        // Always return the response, message will be set in controller
        return ApiResponse.success("Lấy giỏ hàng thành công", response);
    }

    /**
     * Add item to cart
     */
    public ApiResponse<Map<String, Object>> addItemToCart(AddToCartRequest request) {
        User currentUser = authService.getCurrentUser();
        log.info("Adding item to cart: productId={}, colorId={}, quantity={}",
                request.getProduct_id(), request.getColor_id(), request.getQuantity());

        // Validate product exists and is active
        Product product = productRepository.findById(request.getProduct_id())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getIsActive()) {
            throw new BadRequestException("Sản phẩm không còn hoạt động");
        }

        // Validate color exists and belongs to product
        Color color = colorRepository.findById(request.getColor_id())
                .orElseThrow(() -> new ResourceNotFoundException("Color not found"));

        // Check if color is available for this product
        if (!productColorRepository.existsByProductIdAndColorId(request.getProduct_id(), request.getColor_id())) {
            throw new BadRequestException("Màu sắc không khả dụng cho sản phẩm này");
        }

        // Check stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Không đủ hàng trong kho. Chỉ còn " + product.getStockQuantity() + " sản phẩm");
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        // Check if item already exists
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProductAndColor(cart, product, color);
        
        CartItem cartItem;
        String action;
        
        if (existingItemOpt.isPresent()) {
            // Update existing item
            cartItem = existingItemOpt.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Check stock again with new quantity
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(
                        "Không đủ hàng trong kho. Chỉ còn " + product.getStockQuantity() + " sản phẩm");
            }
            
            cartItem.setQuantity(newQuantity);
            cartItem = cartItemRepository.save(cartItem);
            action = "updated";
            log.info("Updated existing cart item quantity to: {}", newQuantity);
        } else {
            // Create new item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setColor(color);
            cartItem.setQuantity(request.getQuantity());
            
            // Set unit price (current price at time of adding)
            BigDecimal currentPrice = product.getDiscountPrice() != null && 
                    product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0 
                    ? product.getDiscountPrice() 
                    : product.getPrice();
            cartItem.setUnitPrice(currentPrice);
            
            cartItem = cartItemRepository.save(cartItem);
            action = "added";
            log.info("Added new item to cart");
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("cart_item", buildCartItemDetail(cartItem));
        responseData.put("action", action);

        String message = "added".equals(action) 
                ? "Thêm sản phẩm vào giỏ hàng thành công"
                : "Cập nhật số lượng sản phẩm trong giỏ hàng";
        
        return ApiResponse.success(message, responseData);
    }

    /**
     * Update cart item quantity
     */
    public ApiResponse<Map<String, Object>> updateQuantity(Long itemId, UpdateQuantityRequest request) {
        User currentUser = authService.getCurrentUser();
        log.info("Updating quantity for cart item {} to {}", itemId, request.getQuantity());

        CartItem cartItem = findAndVerifyCartItem(itemId, currentUser);
        Product product = cartItem.getProduct();

        // If quantity is 0, delete item
        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deleted_item_id", itemId);
            return ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng", responseData);
        }

        // Check stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Không đủ hàng trong kho. Chỉ còn " + product.getStockQuantity() + " sản phẩm");
        }

        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("id", cartItem.getId());
        itemData.put("quantity", cartItem.getQuantity());
        itemData.put("line_total", cartItem.getTotalPrice());
        itemData.put("is_available", isItemAvailable(cartItem));
        itemData.put("stock_status", getStockStatus(cartItem));
        responseData.put("cart_item", itemData);

        return ApiResponse.success("Cập nhật số lượng thành công", responseData);
    }

    /**
     * Update cart item color
     */
    public ApiResponse<Map<String, Object>> updateColor(Long itemId, UpdateColorRequest request) {
        User currentUser = authService.getCurrentUser();
        log.info("Updating color for cart item {} to colorId {}", itemId, request.getColor_id());

        CartItem cartItem = findAndVerifyCartItem(itemId, currentUser);
        Product product = cartItem.getProduct();

        // Validate new color exists and belongs to product
        Color newColor = colorRepository.findById(request.getColor_id())
                .orElseThrow(() -> new ResourceNotFoundException("Color not found"));

        if (!productColorRepository.existsByProductIdAndColorId(product.getId(), request.getColor_id())) {
            throw new BadRequestException("Màu sắc không khả dụng cho sản phẩm này");
        }

        // Check if same product+color already exists in cart
        Cart cart = cartItem.getCart();
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductAndColor(cart, product, newColor);
        
        if (existingItem.isPresent() && !existingItem.get().getId().equals(itemId)) {
            // Merge with existing item
            CartItem existing = existingItem.get();
            existing.setQuantity(existing.getQuantity() + cartItem.getQuantity());
            cartItemRepository.save(existing);
        cartItemRepository.delete(cartItem);
            cartItem = existing;
        } else {
            // Just update color
            cartItem.setColor(newColor);
            cartItem = cartItemRepository.save(cartItem);
        }

        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("id", cartItem.getId());
        
        Map<String, Object> colorData = new HashMap<>();
        colorData.put("id", newColor.getId());
        colorData.put("color_name", newColor.getColorName());
        colorData.put("hex_code", newColor.getHexCode());
        itemData.put("color", colorData);
        
        itemData.put("is_available", isItemAvailable(cartItem));
        itemData.put("stock_status", getStockStatus(cartItem));
        responseData.put("cart_item", itemData);

        return ApiResponse.success("Thay đổi màu sắc thành công", responseData);
    }

    /**
     * Remove cart item
     */
    public ApiResponse<Map<String, Object>> removeItem(Long itemId) {
        User currentUser = authService.getCurrentUser();
        log.info("Removing cart item: {}", itemId);

        CartItem cartItem = findAndVerifyCartItem(itemId, currentUser);
        cartItemRepository.delete(cartItem);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("deleted_item_id", itemId);

        return ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công", responseData);
    }

    /**
     * Clear cart
     */
    public ApiResponse<Map<String, Object>> clearCart() {
        User currentUser = authService.getCurrentUser();
        log.info("Clearing cart for user: {}", currentUser.getUsername());

        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);
        int clearedCount = 0;

        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCart(cart);
            clearedCount = items.size();
            cartItemRepository.deleteByCart(cart);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("cleared_items_count", clearedCount);

        return ApiResponse.success("Xóa toàn bộ giỏ hàng thành công", responseData);
    }

    /**
     * Validate cart
     */
    @Transactional(readOnly = true)
    public ApiResponse<CartValidateResponse> validateCart() {
        User currentUser = authService.getCurrentUser();
        log.info("Validating cart for user: {}", currentUser.getUsername());

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        List<CartItem> items = cartItemRepository.findByCart(cart);
        
        List<CartValidateResponse.PriceChange> priceChanges = new ArrayList<>();
        List<CartValidateResponse.StockIssue> stockIssues = new ArrayList<>();
        List<CartValidateResponse.UnavailableProduct> unavailableProducts = new ArrayList<>();
        
        int availableItems = 0;
        int unavailableItems = 0;

        for (CartItem item : items) {
            Product product = item.getProduct();
            BigDecimal currentPrice = product.getDiscountPrice() != null && 
                    product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0 
                    ? product.getDiscountPrice() 
                    : product.getPrice();

            // Check price changes
            if (item.getUnitPrice().compareTo(currentPrice) != 0) {
                CartValidateResponse.PriceChange priceChange = CartValidateResponse.PriceChange.builder()
                        .item_id(item.getId())
                        .product_name(product.getName())
                        .old_price(item.getUnitPrice())
                        .new_price(currentPrice)
                        .updated(false)
                        .build();
                priceChanges.add(priceChange);
                
                // Update price in cart
                item.setUnitPrice(currentPrice);
                cartItemRepository.save(item);
                priceChange.setUpdated(true);
            }

            // Check stock issues
            if (!product.getIsActive()) {
                unavailableProducts.add(CartValidateResponse.UnavailableProduct.builder()
                        .item_id(item.getId())
                        .product_name(product.getName())
                        .issue("product_inactive")
                        .build());
                unavailableItems++;
            } else if (product.getStockQuantity() < item.getQuantity()) {
                stockIssues.add(CartValidateResponse.StockIssue.builder()
                        .item_id(item.getId())
                        .product_name(product.getName())
                        .requested_quantity(item.getQuantity())
                        .available_quantity(product.getStockQuantity())
                        .issue("insufficient_stock")
                        .build());
                unavailableItems++;
            } else {
                availableItems++;
            }
        }

        boolean isValid = priceChanges.isEmpty() && stockIssues.isEmpty() && unavailableProducts.isEmpty();

        CartValidateResponse response = CartValidateResponse.builder()
                .is_valid(isValid)
                .total_items(items.size())
                .available_items(availableItems)
                .unavailable_items(unavailableItems)
                .price_changes(priceChanges)
                .stock_issues(stockIssues)
                .unavailable_products(unavailableProducts)
                .build();

        return ApiResponse.success("Kiểm tra giỏ hàng thành công", response);
    }

    /**
     * Sync guest cart
     */
    public ApiResponse<CartSyncResponse> syncCart(SyncCartRequest request) {
        User currentUser = authService.getCurrentUser();
        log.info("Syncing guest cart for user: {}", currentUser.getUsername());

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        int syncedItems = 0;
        int mergedItems = 0;
        int newItems = 0;
        int failedItems = 0;

        for (SyncCartRequest.GuestCartItem guestItem : request.getGuest_cart_items()) {
            try {
                Product product = productRepository.findById(guestItem.getProduct_id())
                        .orElse(null);
                
                if (product == null || !product.getIsActive()) {
                    failedItems++;
                    continue;
                }

                Color color = colorRepository.findById(guestItem.getColor_id())
                        .orElse(null);

                if (color == null || !productColorRepository.existsByProductIdAndColorId(
                        guestItem.getProduct_id(), guestItem.getColor_id())) {
                    failedItems++;
                    continue;
                }

                // Check if item already exists
                Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductAndColor(
                        cart, product, color);

                if (existingItem.isPresent()) {
                    // Merge quantities
                    CartItem item = existingItem.get();
                    int newQuantity = item.getQuantity() + guestItem.getQuantity();
                    if (product.getStockQuantity() >= newQuantity) {
                        item.setQuantity(newQuantity);
                        cartItemRepository.save(item);
                        mergedItems++;
                    } else {
                        failedItems++;
                    }
                } else {
                    // Create new item
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setColor(color);
                    newItem.setQuantity(guestItem.getQuantity());
                    
                    BigDecimal currentPrice = product.getDiscountPrice() != null && 
                            product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0 
                            ? product.getDiscountPrice() 
                            : product.getPrice();
                    newItem.setUnitPrice(currentPrice);
                    
                    cartItemRepository.save(newItem);
                    newItems++;
                }
                syncedItems++;
            } catch (Exception e) {
                log.error("Failed to sync cart item: {}", e.getMessage());
                failedItems++;
            }
        }

        List<CartItem> allItems = cartItemRepository.findByCart(cart);
        int totalItems = allItems.size();
        int totalQuantity = allItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        CartSyncResponse.CartSummary summary = CartSyncResponse.CartSummary.builder()
                .total_items(totalItems)
                .total_quantity(totalQuantity)
                .build();

        CartSyncResponse response = CartSyncResponse.builder()
                .synced_items(syncedItems)
                .merged_items(mergedItems)
                .new_items(newItems)
                .failed_items(failedItems)
                .cart_summary(summary)
                .build();

        return ApiResponse.success("Đồng bộ giỏ hàng thành công", response);
    }

    /**
     * Get cart count
     */
    @Transactional(readOnly = true)
    public ApiResponse<CartCountResponse> getCartCount() {
        User currentUser = authService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser));

        List<CartItem> items = cartItemRepository.findByCart(cart);
        
        int totalItems = items.size();
        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        int availableItems = 0;
        int unavailableItems = 0;

        for (CartItem item : items) {
            if (isItemAvailable(item)) {
                availableItems++;
            } else {
                unavailableItems++;
            }
        }

        CartCountResponse response = CartCountResponse.builder()
                .total_items(totalItems)
                .total_quantity(totalQuantity)
                .available_items(availableItems)
                .unavailable_items(unavailableItems)
                .build();

        return ApiResponse.success("Lấy số lượng giỏ hàng thành công", response);
    }

    // Helper methods
    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private CartItem findAndVerifyCartItem(Long itemId, User user) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        return cartItem;
    }

    private CartDetailResponse buildCartDetailResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        
        List<CartDetailResponse.CartItemDetail> itemDetails = items.stream()
                .map(this::buildCartItemDetail)
                .collect(Collectors.toList());

        int totalItems = items.size();
        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        boolean hasUnavailableItems = items.stream().anyMatch(item -> !isItemAvailable(item));

        return CartDetailResponse.builder()
                .cart_id(cart.getId())
                .user_id(cart.getUser().getId())
                .items(itemDetails)
                .total_items(totalItems)
                .total_quantity(totalQuantity)
                .has_unavailable_items(hasUnavailableItems)
                .created_at(cart.getCreatedAt())
                .updated_at(cart.getCreatedAt()) // Cart doesn't have updated_at, use created_at
                .build();
    }

    private CartDetailResponse.CartItemDetail buildCartItemDetail(CartItem item) {
        Product product = item.getProduct();
        Color color = item.getColor();

        // Get primary image
        String primaryImage = productImageRepository.findByProductIdAndIsPrimaryTrue(product.getId())
                .map(ProductImage::getImageUrl)
                .orElseGet(() -> {
                    List<ProductImage> images = productImageRepository.findByProductId(product.getId());
                    return images.isEmpty() ? null : images.get(0).getImageUrl();
                });

        BigDecimal currentPrice = product.getDiscountPrice() != null && 
                product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0 
                ? product.getDiscountPrice() 
                : product.getPrice();

        CartDetailResponse.ProductInfo productInfo = CartDetailResponse.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .price(product.getPrice())
                .discount_price(product.getDiscountPrice())
                .current_price(currentPrice)
                .stock_quantity(product.getStockQuantity())
                .primary_image(primaryImage)
                .is_active(product.getIsActive())
                .build();

        CartDetailResponse.ColorInfo colorInfo = CartDetailResponse.ColorInfo.builder()
                .id(color.getId())
                .color_name(color.getColorName())
                .hex_code(color.getHexCode())
                .build();

        return CartDetailResponse.CartItemDetail.builder()
                .id(item.getId())
                .product(productInfo)
                .color(colorInfo)
                .quantity(item.getQuantity())
                .unit_price(item.getUnitPrice())
                .line_total(item.getTotalPrice())
                .is_available(isItemAvailable(item))
                .stock_status(getStockStatus(item))
                .build();
    }

    private boolean isItemAvailable(CartItem item) {
        Product product = item.getProduct();
        return product.getIsActive() && product.getStockQuantity() >= item.getQuantity();
    }

    private String getStockStatus(CartItem item) {
        Product product = item.getProduct();
        if (!product.getIsActive()) {
            return "product_inactive";
        }
        if (product.getStockQuantity() == 0) {
            return "out_of_stock";
        }
        if (product.getStockQuantity() < item.getQuantity()) {
            return "insufficient_stock";
        }
        return "in_stock";
    }
}

package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.AddToCartRequest;
import fit.se.be_phone_store.dto.request.UpdateCartItemRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.CartResponse;
import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.entity.Cart;
import fit.se.be_phone_store.entity.CartItem;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.Color;
import fit.se.be_phone_store.repository.CartRepository;
import fit.se.be_phone_store.repository.CartItemRepository;
import fit.se.be_phone_store.repository.ProductRepository;
import fit.se.be_phone_store.repository.ColorRepository;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.InsufficientStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CartService - Handles shopping cart business logic
 * CHỨA @Slf4j ANNOTATION ĐỂ SỬ DỤNG BIẾN log
 */
@Service
@RequiredArgsConstructor
@Slf4j  // ← Đây là annotation quan trọng để có biến log
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final AuthService authService;

    /**
     * Add item to cart
     */
    public ApiResponse<CartResponse> addToCart(AddToCartRequest request) {
        User currentUser = authService.getCurrentUser();
        log.info("Adding item to cart for user: {}, productId: {}, colorId: {}, quantity: {}",
                currentUser.getUsername(), request.getProductId(), request.getColorId(), request.getQuantity());

        // Validate product exists and is active
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Validate color exists
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color not found"));

        // Check if user has existing cart, if not create one
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElse(createNewCart(currentUser));

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartAndProductAndColor(cart, product, color)
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
            log.info("Updated existing cart item quantity to: {}", newQuantity);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setColor(color);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
            log.info("Added new item to cart: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        }

        return ApiResponse.success("Item added to cart successfully", getCartResponse(cart));
    }

    /**
     * Update cart item quantity
     */
    public ApiResponse<CartResponse> updateCartItem(Long cartItemId, UpdateCartItemRequest request) {
        User currentUser = authService.getCurrentUser();
        log.info("Updating cart item {} quantity to: {}", cartItemId, request.getQuantity());

        // Find cart item and verify ownership
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        // If quantity is 0, remove item
        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
        }

        Cart cart = cartItem.getCart();
        log.info("Updated cart item successfully, new quantity: {}", request.getQuantity());

        return ApiResponse.success("Cart item updated successfully", getCartResponse(cart));
    }

    /**
     * Remove item from cart
     */
    public ApiResponse<Void> removeFromCart(Long cartItemId) {
        User currentUser = authService.getCurrentUser();
        log.info("Removing cart item: {}", cartItemId);

        // Find cart item and verify ownership
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item successfully: {}", cartItemId);

        return ApiResponse.success("Item removed from cart successfully");
    }

    /**
     * Get current user's cart
     */
    @Transactional(readOnly = true)
    public ApiResponse<CartResponse> getCart() {
        User currentUser = authService.getCurrentUser();

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElse(createNewCart(currentUser));

        return ApiResponse.success("Cart retrieved successfully", getCartResponse(cart));
    }

    /**
     * Clear cart
     */
    public ApiResponse<Void> clearCart() {
        User currentUser = authService.getCurrentUser();
        log.info("Clearing cart for user: {}", currentUser.getUsername());

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElse(null);

        if (cart != null) {
            cartItemRepository.deleteByCart(cart);
        }

        log.info("Cart cleared successfully");
        return ApiResponse.success("Cart cleared successfully");
    }

    // Helper methods
    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private CartResponse getCartResponse(Cart cart) {
        // Implementation for converting Cart entity to CartResponse
        // This would need to be implemented based on your CartResponse structure
        return new CartResponse(); // Placeholder
    }
}
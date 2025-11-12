package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.*;
import fit.se.be_phone_store.repository.*;
import fit.se.be_phone_store.dto.request.CreateProductRequest;
import fit.se.be_phone_store.dto.request.UpdateProductRequest;
import fit.se.be_phone_store.dto.request.ProductFilterRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.ProductResponse;
import fit.se.be_phone_store.dto.response.ProductDetailResponse;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import fit.se.be_phone_store.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProductService - Handles product management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ColorRepository colorRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSpecificationRepository specificationRepository;
    private final ProductImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final AuthService authService;

    /**
     * Get all products with pagination
     * @param pageable Pagination parameters
     * @return Paged API response with products
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Getting all active products with pagination");
        
        Page<Product> productsPage = productRepository.findByIsActiveTrue(pageable);
        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);
        
        return PagedApiResponse.success("Products retrieved successfully", responsePage);
    }

    /**
     * Get product by ID with full details
     * @param productId Product ID
     * @return API response with product details
     */
    @Transactional(readOnly = true)
    public ApiResponse<ProductDetailResponse> getProductById(Long productId) {
        log.info("Getting product details for ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!product.getIsActive()) {
            throw new ResourceNotFoundException("Product is not available");
        }
        
        ProductDetailResponse response = mapToProductDetailResponse(product);
        return ApiResponse.success("Product retrieved successfully", response);
    }

    /**
     * Get product by slug
     * @param slug Product slug
     * @return API response with product details
     */
    @Transactional(readOnly = true)
    public ApiResponse<ProductDetailResponse> getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);
        
        Product product = productRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!product.getIsActive()) {
            throw new ResourceNotFoundException("Product is not available");
        }
        
        ProductDetailResponse response = mapToProductDetailResponse(product);
        return ApiResponse.success("Product retrieved successfully", response);
    }

    /**
     * Search products by keyword
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Paged API response with products
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        log.info("Searching products with keyword: {}", keyword);
        
        Page<Product> productsPage = productRepository.searchProducts(keyword, pageable);
        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);
        
        return PagedApiResponse.success("Search results retrieved successfully", responsePage);
    }

    /**
     * Filter products by multiple criteria
     * @param filterRequest Filter criteria
     * @param pageable Pagination parameters
     * @return Paged API response with filtered products
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> filterProducts(ProductFilterRequest filterRequest, Pageable pageable) {
        log.info("Filtering products with criteria: {}", filterRequest);
        
        Page<Product> productsPage = productRepository.findProductsWithFilters(
            filterRequest.getCategoryId(),
            filterRequest.getBrandId(),
            filterRequest.getColorId(),
            filterRequest.getMinPrice(),
            filterRequest.getMaxPrice(),
            filterRequest.getInStock(),
            pageable
        );
        
        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);
        
        return PagedApiResponse.success("Filtered products retrieved successfully", responsePage);
    }

    /**
     * Get products by category
     * @param categoryId Category ID
     * @param pageable Pagination parameters
     * @return Paged API response with products
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Getting products by category ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        Page<Product> productsPage = productRepository.findByCategoryAndIsActiveTrue(category, pageable);
        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);
        
        return PagedApiResponse.success("Products retrieved successfully", responsePage);
    }

    /**
     * Get products by brand
     * @param brandId Brand ID
     * @param pageable Pagination parameters
     * @return Paged API response with products
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> getProductsByBrand(Long brandId, Pageable pageable) {
        log.info("Getting products by brand ID: {}", brandId);
        
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        
        Page<Product> productsPage = productRepository.findByBrandAndIsActiveTrue(brand, pageable);
        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);
        
        return PagedApiResponse.success("Products retrieved successfully", responsePage);
    }

    /**
     * Create new product (Admin only)
     * @param request Create product request
     * @return API response with created product data
     */
    public ApiResponse<Map<String, Object>> createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }
        
        // Validate category, brand, color exist
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new BadRequestException("Category not found"));
        
        Brand brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new BadRequestException("Brand not found"));
        
        Color color = colorRepository.findById(request.getColorId())
            .orElseThrow(() -> new BadRequestException("Color not found"));
        
        // Create product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setBrand(brand);
        product.setColor(color);
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        Product savedProduct = productRepository.save(product);
        
        // Create response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("productId", savedProduct.getId());
        responseData.put("slug", savedProduct.getSlug());
        responseData.put("name", savedProduct.getName());
        
        log.info("Product created successfully: {}", savedProduct.getId());
        return ApiResponse.success("Product created successfully", responseData);
    }

    /**
     * Update product (Admin only)
     * @param productId Product ID
     * @param request Update product request
     * @return API response with updated product data
     */
    public ApiResponse<Map<String, Object>> updateProduct(Long productId, UpdateProductRequest request) {
        log.info("Updating product: {}", productId);
        
        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Update fields if provided
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDiscountPrice() != null) {
            product.setDiscountPrice(request.getDiscountPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found"));
            product.setCategory(category);
        }
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new BadRequestException("Brand not found"));
            product.setBrand(brand);
        }
        if (request.getColorId() != null) {
            Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new BadRequestException("Color not found"));
            product.setColor(color);
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        
        Product updatedProduct = productRepository.save(product);
        
        // Create response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("productId", updatedProduct.getId());
        responseData.put("name", updatedProduct.getName());
        
        log.info("Product updated successfully: {}", productId);
        return ApiResponse.success("Product updated successfully", responseData);
    }

    /**
     * Delete product (Admin only) - Soft delete
     * @param productId Product ID
     * @return API response
     */
    public ApiResponse<Void> deleteProduct(Long productId) {
        log.info("Deleting product: {}", productId);
        
        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Soft delete - set isActive to false
        product.setIsActive(false);
        productRepository.save(product);
        
        log.info("Product deleted successfully: {}", productId);
        return ApiResponse.success("Product deleted successfully");
    }

    /**
     * Get product specifications
     * @param productId Product ID
     * @return API response with specifications
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, String>>> getProductSpecifications(Long productId) {
        log.info("Getting specifications for product: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        List<ProductSpecification> specifications = specificationRepository.findByProduct(product);
        List<Map<String, String>> specList = specifications.stream()
            .map(spec -> {
                Map<String, String> specMap = new HashMap<>();
                specMap.put("name", spec.getSpecName());
                specMap.put("value", spec.getSpecValue());
                return specMap;
            })
            .collect(Collectors.toList());
        
        return ApiResponse.success("Product specifications retrieved successfully", specList);
    }

    /**
     * Get available colors for product
     * @param productId Product ID
     * @return API response with available colors
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> getProductColors(Long productId) {
        log.info("Getting available colors for product: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        List<Color> colors = productColorRepository.findColorsByProductId(productId);
        List<Map<String, Object>> colorList = colors.stream()
            .map(color -> {
                Map<String, Object> colorMap = new HashMap<>();
                colorMap.put("id", color.getId());
                colorMap.put("colorName", color.getColorName());
                colorMap.put("hexCode", color.getHexCode());
                return colorMap;
            })
            .collect(Collectors.toList());
        
        return ApiResponse.success("Product colors retrieved successfully", colorList);
    }

    /**
     * Get related products (same category, different product)
     * @param productId Product ID
     * @param pageable Pagination parameters
     * @return API response with related products
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<ProductResponse>> getRelatedProducts(Long productId, Pageable pageable) {
        log.info("Getting related products for: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        List<Product> relatedProducts = productRepository.findRelatedProducts(
            product.getCategory(), productId, pageable);
        
        List<ProductResponse> responseList = relatedProducts.stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
        
        return ApiResponse.success("Related products retrieved successfully", responseList);
    }

    /**
     * Get product statistics (Admin only)
     * @return API response with product statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getProductStatistics() {
        log.info("Getting product statistics");
        
        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalProducts", productRepository.countActiveProducts());
        statistics.put("inStockProducts", productRepository.countInStockProducts());
        statistics.put("outOfStockProducts", productRepository.findOutOfStockProducts().size());
        statistics.put("lowStockProducts", productRepository.findLowStockProducts(10).size());
        statistics.put("minPrice", productRepository.findMinPrice());
        statistics.put("maxPrice", productRepository.findMaxPrice());
        statistics.put("averagePrice", calculateAveragePrice());
        
        // Product count by category
        List<Object[]> categoryStats = productRepository.countProductsByCategory();
        Map<String, Long> categoryCount = new HashMap<>();
        categoryStats.forEach(stat -> categoryCount.put((String) stat[0], (Long) stat[1]));
        statistics.put("productsByCategory", categoryCount);
        
        // Product count by brand
        List<Object[]> brandStats = productRepository.countProductsByBrand();
        Map<String, Long> brandCount = new HashMap<>();
        brandStats.forEach(stat -> brandCount.put((String) stat[0], (Long) stat[1]));
        statistics.put("productsByBrand", brandCount);
        
        return ApiResponse.success("Product statistics retrieved successfully", statistics);
    }

    /**
     * Update product stock (Admin only)
     * @param productId Product ID
     * @param quantity New stock quantity
     * @return API response
     */
    public ApiResponse<Map<String, Integer>> updateProductStock(Long productId, Integer quantity) {
        log.info("Updating stock for product {}: {}", productId, quantity);
        
        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        product.setStockQuantity(quantity);
        productRepository.save(product);
        
        Map<String, Integer> responseData = new HashMap<>();
        responseData.put("productId", productId.intValue());
        responseData.put("newStock", quantity);
        
        log.info("Product stock updated successfully: {}", productId);
        return ApiResponse.success("Product stock updated successfully", responseData);
    }

    /**
     * Calculate average price of all active products
     */
    private BigDecimal calculateAveragePrice() {
        List<Product> products = productRepository.findByIsActiveTrue();
        if (products.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = products.stream()
            .map(Product::getEffectivePrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(products.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .slug(product.getSlug())
            .description(product.getDescription())
            .price(product.getPrice())
            .discountPrice(product.getDiscountPrice())
            .stockQuantity(product.getStockQuantity())
            .isActive(product.getIsActive())
            .categoryName(product.getCategory().getName().name())
            .brandName(product.getBrand().getName())
            .colorName(product.getColor().getColorName())
            .mainImageUrl(getMainImageUrl(product))
            .hasDiscount(product.hasDiscount())
            .inStock(product.isInStock())
            .createdAt(product.getCreatedAt())
            .build();
    }

    /**
     * Map Product entity to ProductDetailResponse DTO
     */
    private ProductDetailResponse mapToProductDetailResponse(Product product) {
        // Get specifications
        List<ProductSpecification> specifications = specificationRepository.findByProduct(product);
        
        // Get available colors
        List<Color> availableColors = productColorRepository.findColorsByProductId(product.getId());
        
        // Get images
        List<ProductImage> images = imageRepository.findByProduct(product);
        
        // Get review statistics
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(product.getId());
        Long reviewCount = reviewRepository.countByProductId(product.getId());
        
        return ProductDetailResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .slug(product.getSlug())
            .description(product.getDescription())
            .price(product.getPrice())
            .discountPrice(product.getDiscountPrice())
            .stockQuantity(product.getStockQuantity())
            .isActive(product.getIsActive())
            .category(mapCategoryInfo(product.getCategory()))
            .brand(mapBrandInfo(product.getBrand()))
            .defaultColor(mapColorInfo(product.getColor()))
            .availableColors(availableColors.stream().map(this::mapColorInfo).collect(Collectors.toList()))
            .specifications(specifications.stream().map(this::mapSpecificationInfo).collect(Collectors.toList()))
            .images(images.stream().map(this::mapImageInfo).collect(Collectors.toList()))
            .averageRating(averageRating != null ? averageRating : 0.0)
            .reviewCount(reviewCount.intValue())
            .hasDiscount(product.hasDiscount())
            .inStock(product.isInStock())
            .createdAt(product.getCreatedAt())
            .build();
    }

    private String getMainImageUrl(Product product) {
        return imageRepository.findByProductAndIsPrimaryTrue(product)
            .map(ProductImage::getImageUrl)
            .orElse(null);
    }

    private Map<String, Object> mapCategoryInfo(Category category) {
        Map<String, Object> categoryInfo = new HashMap<>();
        categoryInfo.put("id", category.getId());
        categoryInfo.put("name", category.getName().name());
        categoryInfo.put("description", category.getDescription());
        return categoryInfo;
    }

    private Map<String, Object> mapBrandInfo(Brand brand) {
        Map<String, Object> brandInfo = new HashMap<>();
        brandInfo.put("id", brand.getId());
        brandInfo.put("name", brand.getName());
        brandInfo.put("description", brand.getDescription());
        return brandInfo;
    }

    private Map<String, Object> mapColorInfo(Color color) {
        Map<String, Object> colorInfo = new HashMap<>();
        colorInfo.put("id", color.getId());
        colorInfo.put("colorName", color.getColorName());
        colorInfo.put("hexCode", color.getHexCode());
        return colorInfo;
    }

    private Map<String, String> mapSpecificationInfo(ProductSpecification spec) {
        Map<String, String> specInfo = new HashMap<>();
        specInfo.put("name", spec.getSpecName());
        specInfo.put("value", spec.getSpecValue());
        return specInfo;
    }

    private Map<String, Object> mapImageInfo(ProductImage image) {
        Map<String, Object> imageInfo = new HashMap<>();
        imageInfo.put("id", image.getId());
        imageInfo.put("imageUrl", image.getImageUrl());
        imageInfo.put("altText", image.getAltText());
        imageInfo.put("isPrimary", image.getIsPrimary());
        return imageInfo;
    }
}

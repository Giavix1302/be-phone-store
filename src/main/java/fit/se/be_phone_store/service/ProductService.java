package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.product.*;
import fit.se.be_phone_store.dto.request.product.ProductFilterRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.product.ProductResponse;
import fit.se.be_phone_store.dto.response.product.StockUpdateResponse;
import fit.se.be_phone_store.entity.*;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.UnauthorizedException;
import fit.se.be_phone_store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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
    private final ProductImageRepository productImageRepository;
    private final ProductSpecificationRepository productSpecificationRepository;
    private final ReviewRepository reviewRepository;
    private final CloudinaryService cloudinaryService;
    private final AuthService authService;

    /**
     * Get products list with search, filter and pagination (User API)
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> getProducts(ProductFilterRequest filters) {
        log.info("Getting products with filters: {}", filters);

        // Build pageable
        Sort sort = buildSort(filters.getSortBy(), filters.getSortOrder());
        Pageable pageable = PageRequest.of(filters.getPage() - 1, filters.getLimit(), sort);

        Page<Product> productsPage;

        // Apply filters
        if (hasFilters(filters)) {
            productsPage = productRepository.findProductsWithFilters(
                    getCategoryIdFromName(filters.getCategory()),
                    filters.getBrandId(),
                    filters.getColorId(),
                    filters.getMinPrice(),
                    filters.getMaxPrice(),
                    filters.getInStock(),
                    pageable
            );

            // Apply search if needed
            if (filters.getSearch() != null && !filters.getSearch().trim().isEmpty()) {
                productsPage = productRepository.searchProducts(filters.getSearch(), pageable);
            }
        } else if (filters.getSearch() != null && !filters.getSearch().trim().isEmpty()) {
            productsPage = productRepository.searchProducts(filters.getSearch(), pageable);
        } else {
            productsPage = productRepository.findByIsActiveTrue(pageable);
        }

        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);

        return PagedApiResponse.success("Lấy danh sách sản phẩm thành công", responsePage);
    }

    /**
     * Get product detail by slug (User API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<ProductResponse> getProductDetail(String slug) {
        log.info("Getting product detail for slug: {}", slug);

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        if (!product.getIsActive()) {
            throw new ResourceNotFoundException("Sản phẩm không khả dụng");
        }

        ProductResponse response = mapToProductDetailResponse(product);

        return ApiResponse.success("Lấy chi tiết sản phẩm thành công", response);
    }

    /**
     * Create new product (Admin API)
     */
    public ApiResponse<ProductResponse> createProduct(CreateProductRequest request, MultipartFile[] images) {
        log.info("Creating new product: {}", request.getName());

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        // Validate references exist
        validateProductReferences(request.getCategoryId(), request.getBrandId(),
                request.getColorId(), request.getColorIds());

        // Check if slug already exists
        String slug = generateSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        // Create product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsActive(request.getIsActive());

        // Set references
        product.setCategory(categoryRepository.findById(request.getCategoryId()).get());
        product.setBrand(brandRepository.findById(request.getBrandId()).get());
        product.setColor(colorRepository.findById(request.getColorId()).get());

        Product savedProduct = productRepository.save(product);

        // Add available colors
        addColorsToProduct(savedProduct, request.getColorIds());

        // Upload and save images
        if (images != null && images.length > 0) {
            uploadProductImages(savedProduct, images, request.getImageAlts(), request.getPrimaryImageIndex());
        }

        ProductResponse response = mapToProductResponse(savedProduct);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return ApiResponse.success("Tạo sản phẩm thành công", response);
    }

    /**
     * Update product (Admin API)
     */
    public ApiResponse<ProductResponse> updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product ID: {}", id);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        // Validate references
        validateProductReferences(request.getCategoryId(), request.getBrandId(), request.getColorId(), null);

        // Update basic info
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setIsActive(request.getIsActive());

        // Update slug if name changed
        if (!product.getName().equals(request.getName())) {
            String newSlug = generateSlug(request.getName());
            if (!newSlug.equals(product.getSlug()) && productRepository.existsBySlug(newSlug)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            product.setSlug(newSlug);
        }

        // Update references
        product.setCategory(categoryRepository.findById(request.getCategoryId()).get());
        product.setBrand(brandRepository.findById(request.getBrandId()).get());
        product.setColor(colorRepository.findById(request.getColorId()).get());

        Product updatedProduct = productRepository.save(product);
        ProductResponse response = mapToProductResponse(updatedProduct);

        log.info("Product updated successfully: {}", updatedProduct.getId());
        return ApiResponse.success("Cập nhật sản phẩm thành công", response);
    }

    /**
     * Delete product (Admin API)
     */
    public ApiResponse<Map<String, Object>> deleteProduct(Long id) {
        log.info("Deleting product ID: {}", id);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        // Delete associated images from Cloudinary
        List<ProductImage> images = productImageRepository.findByProduct(product);
        for (ProductImage image : images) {
            cloudinaryService.deleteImage(image.getImageUrl());
        }

        productRepository.delete(product);

        Map<String, Object> response = new HashMap<>();
        response.put("deleted_product_id", id);

        log.info("Product deleted successfully: {}", id);
        return ApiResponse.success("Xóa sản phẩm thành công", response);
    }

    /**
     * Update product stock (Admin API)
     */
    public ApiResponse<StockUpdateResponse> updateStock(Long id, UpdateStockRequest request) {
        log.info("Updating stock for product ID: {}, operation: {}, quantity: {}",
                id, request.getOperation(), request.getStockQuantity());

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        Integer oldStock = product.getStockQuantity();
        Integer newStock;

        switch (request.getOperation().toLowerCase()) {
            case "set":
                newStock = request.getStockQuantity();
                break;
            case "add":
                newStock = oldStock + request.getStockQuantity();
                break;
            case "subtract":
                newStock = oldStock - request.getStockQuantity();
                if (newStock < 0) {
                    throw new BadRequestException("Không thể giảm tồn kho xuống dưới 0");
                }
                break;
            default:
                throw new BadRequestException("Operation không hợp lệ: " + request.getOperation());
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        StockUpdateResponse response = StockUpdateResponse.builder()
                .productId(id)
                .oldStock(oldStock)
                .newStock(newStock)
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Stock updated successfully for product {}: {} -> {}", id, oldStock, newStock);
        return ApiResponse.success("Cập nhật tồn kho thành công", response);
    }

    /**
     * Add colors to product (Admin API)
     */
    public ApiResponse<Map<String, Object>> addColorsToProduct(Long productId, ManageProductColorsRequest request) {
        log.info("Adding colors to product ID: {}", productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        List<Color> addedColors = new ArrayList<>();

        for (Long colorId : request.getColorIds()) {
            Color color = colorRepository.findById(colorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Màu không tồn tại: " + colorId));

            // Check if already exists
            if (!productColorRepository.existsByProductIdAndColorId(productId, colorId)) {
                ProductColor productColor = new ProductColor();
                productColor.setProduct(product);
                productColor.setColor(color);
                productColorRepository.save(productColor);
                addedColors.add(color);
            }
        }

        // FIXED: Line 319 - Explicitly cast to avoid type inference issue
        List<Map<String, Object>> addedColorsInfo = addedColors.stream()
                .map(color -> {
                    Map<String, Object> colorMap = new HashMap<>();
                    colorMap.put("id", color.getId());
                    colorMap.put("color_name", color.getColorName());
                    colorMap.put("hex_code", color.getHexCode());
                    return colorMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("product_id", productId);
        response.put("added_colors", addedColorsInfo);

        return ApiResponse.success("Thêm màu cho sản phẩm thành công", response);
    }

    /**
     * Remove colors from product (Admin API)
     */
    public ApiResponse<Map<String, Object>> removeColorsFromProduct(Long productId, ManageProductColorsRequest request) {
        log.info("Removing colors from product ID: {}", productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        for (Long colorId : request.getColorIds()) {
            // Don't remove default color
            if (product.getColor().getId().equals(colorId)) {
                throw new BadRequestException("Không thể xóa màu mặc định của sản phẩm");
            }

            productColorRepository.deleteByProductIdAndColorId(productId, colorId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("product_id", productId);
        response.put("removed_color_ids", request.getColorIds());

        return ApiResponse.success("Xóa màu khỏi sản phẩm thành công", response);
    }

    /**
     * Add images to product (Admin API)
     */
    public ApiResponse<Map<String, Object>> addImagesToProduct(Long productId, MultipartFile[] images,
                                                               List<String> imageAlts) {
        log.info("Adding images to product ID: {}", productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        List<Map<String, Object>> addedImages = uploadProductImages(product, images, imageAlts, null);

        Map<String, Object> response = new HashMap<>();
        response.put("product_id", productId);
        response.put("added_images", addedImages);

        return ApiResponse.success("Thêm hình ảnh thành công", response);
    }

    /**
     * Update image (Admin API)
     * FIXED: Use Optional instead of List for findByProductIdAndIsPrimaryTrue
     */
    public ApiResponse<Map<String, Object>> updateImage(Long productId, Long imageId, UpdateImageRequest request) {
        log.info("Updating image ID: {} for product ID: {}", imageId, productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Hình ảnh không tồn tại"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Hình ảnh không thuộc về sản phẩm này");
        }

        // Update alt text if provided
        if (request.getAltText() != null) {
            image.setAltText(request.getAltText());
        }

        // Update primary status if provided
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            // FIXED: findByProductIdAndIsPrimaryTrue returns Optional<ProductImage>
            // Set the current primary image to non-primary (if exists)
            Optional<ProductImage> currentPrimaryImage = productImageRepository
                    .findByProductIdAndIsPrimaryTrue(image.getProduct().getId());

            if (currentPrimaryImage.isPresent() && !currentPrimaryImage.get().getId().equals(imageId)) {
                currentPrimaryImage.get().setIsPrimary(false);
                productImageRepository.save(currentPrimaryImage.get());
            }

            // Set this image as primary
            image.setIsPrimary(true);
        }

        ProductImage updatedImage = productImageRepository.save(image);

        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedImage.getId());
        response.put("image_url", updatedImage.getImageUrl());
        response.put("alt_text", updatedImage.getAltText());
        response.put("is_primary", updatedImage.getIsPrimary());

        return ApiResponse.success("Cập nhật hình ảnh thành công", response);
    }

    /**
     * Delete image (Admin API)
     */
    public ApiResponse<Map<String, Object>> deleteImage(Long productId, Long imageId) {
        log.info("Deleting image ID: {} from product ID: {}", imageId, productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Hình ảnh không tồn tại"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Hình ảnh không thuộc về sản phẩm này");
        }

        // Delete from Cloudinary
        cloudinaryService.deleteImage(image.getImageUrl());

        // Delete from database
        productImageRepository.delete(image);

        Map<String, Object> response = new HashMap<>();
        response.put("deleted_image_id", imageId);

        return ApiResponse.success("Xóa hình ảnh thành công", response);
    }

    /**
     * Add specifications to product (Admin API)
     */
    public ApiResponse<Map<String, Object>> addSpecificationsToProduct(Long productId, ManageSpecificationsRequest request) {
        log.info("Adding specifications to product ID: {}", productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        List<Map<String, Object>> addedSpecs = new ArrayList<>();

        for (ManageSpecificationsRequest.SpecificationItem specItem : request.getSpecifications()) {
            // Check if spec already exists
            Optional<ProductSpecification> existingSpec = productSpecificationRepository
                    .findByProductAndSpecName(product, specItem.getSpecName());

            if (existingSpec.isPresent()) {
                // Update existing spec
                existingSpec.get().setSpecValue(specItem.getSpecValue());
                productSpecificationRepository.save(existingSpec.get());
            } else {
                // Create new spec
                ProductSpecification spec = new ProductSpecification();
                spec.setProduct(product);
                spec.setSpecName(specItem.getSpecName());
                spec.setSpecValue(specItem.getSpecValue());

                ProductSpecification savedSpec = productSpecificationRepository.save(spec);

                Map<String, Object> specMap = new HashMap<>();
                specMap.put("id", savedSpec.getId());
                specMap.put("spec_name", savedSpec.getSpecName());
                specMap.put("spec_value", savedSpec.getSpecValue());
                addedSpecs.add(specMap);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("product_id", productId);
        response.put("added_specifications", addedSpecs);

        return ApiResponse.success("Thêm thông số kỹ thuật thành công", response);
    }

    /**
     * Update specification (Admin API)
     */
    public ApiResponse<Map<String, Object>> updateSpecification(Long productId, Long specId,
                                                                UpdateSpecificationRequest request) {
        log.info("Updating specification ID: {} for product ID: {}", specId, productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        ProductSpecification spec = productSpecificationRepository.findById(specId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông số không tồn tại"));

        if (!spec.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Thông số không thuộc về sản phẩm này");
        }

        spec.setSpecName(request.getSpecName());
        spec.setSpecValue(request.getSpecValue());

        ProductSpecification updatedSpec = productSpecificationRepository.save(spec);

        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedSpec.getId());
        response.put("spec_name", updatedSpec.getSpecName());
        response.put("spec_value", updatedSpec.getSpecValue());

        return ApiResponse.success("Cập nhật thông số thành công", response);
    }

    /**
     * Delete specification (Admin API)
     */
    public ApiResponse<Map<String, Object>> deleteSpecification(Long productId, Long specId) {
        log.info("Deleting specification ID: {} from product ID: {}", specId, productId);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        ProductSpecification spec = productSpecificationRepository.findById(specId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông số không tồn tại"));

        if (!spec.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Thông số không thuộc về sản phẩm này");
        }

        productSpecificationRepository.delete(spec);

        Map<String, Object> response = new HashMap<>();
        response.put("deleted_specification_id", specId);

        return ApiResponse.success("Xóa thông số thành công", response);
    }

    /**
     * Get all products (Admin API)
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ProductResponse> getAllProductsAdmin(ProductFilterRequest filters) {
        log.info("Getting all products for admin with filters: {}", filters);

        // Check admin permission
        if (!authService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }

        // Similar to user API but include inactive products
        Sort sort = buildSort(filters.getSortBy(), filters.getSortOrder());
        Pageable pageable = PageRequest.of(filters.getPage() - 1, filters.getLimit(), sort);

        Page<Product> productsPage = productRepository.findAll(pageable);

        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);

        return PagedApiResponse.success("Lấy danh sách sản phẩm thành công", responsePage);
    }

    // Private helper methods

    private void validateProductReferences(Long categoryId, Long brandId, Long colorId, List<Long> colorIds) {
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new BadRequestException("Danh mục không tồn tại");
        }

        // Validate brand exists
        if (!brandRepository.existsById(brandId)) {
            throw new BadRequestException("Thương hiệu không tồn tại");
        }

        // Validate default color exists
        if (!colorRepository.existsById(colorId)) {
            throw new BadRequestException("Màu mặc định không tồn tại");
        }

        // Validate all colors exist
        if (colorIds != null) {
            for (Long id : colorIds) {
                if (!colorRepository.existsById(id)) {
                    throw new BadRequestException("Màu không tồn tại: " + id);
                }
            }
        }
    }

    private void addColorsToProduct(Product product, List<Long> colorIds) {
        for (Long colorId : colorIds) {
            if (!productColorRepository.existsByProductIdAndColorId(product.getId(), colorId)) {
                Color color = colorRepository.findById(colorId).get();
                ProductColor productColor = new ProductColor();
                productColor.setProduct(product);
                productColor.setColor(color);
                productColorRepository.save(productColor);
            }
        }
    }

    private List<Map<String, Object>> uploadProductImages(Product product, MultipartFile[] images,
                                                          List<String> imageAlts, Integer primaryIndex) {
        List<Map<String, Object>> uploadedImages = new ArrayList<>();

        if (images == null || images.length == 0) {
            throw new BadRequestException("Phải có ít nhất một hình ảnh");
        }

        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];

            // Upload to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(image, "phone-ecommerce/products");

            // Create ProductImage entity
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(imageUrl);

            if (imageAlts != null && i < imageAlts.size()) {
                productImage.setAltText(imageAlts.get(i));
            }

            boolean isPrimary = (primaryIndex != null && i == primaryIndex) ||
                    (primaryIndex == null && i == 0);
            productImage.setIsPrimary(isPrimary);

            ProductImage savedImage = productImageRepository.save(productImage);

            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("id", savedImage.getId());
            imageMap.put("image_url", savedImage.getImageUrl());
            imageMap.put("alt_text", savedImage.getAltText());
            imageMap.put("is_primary", savedImage.getIsPrimary());
            uploadedImages.add(imageMap);
        }

        return uploadedImages;
    }

    private ProductResponse mapToProductResponse(Product product) {
        // Get primary image
        String primaryImage = productImageRepository.findByProductIdAndIsPrimaryTrue(product.getId())
                .map(ProductImage::getImageUrl)
                .orElse("");

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .stockQuantity(product.getStockQuantity())
                .category(ProductResponse.CategoryInfo.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .brand(ProductResponse.BrandInfo.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .build())
                .defaultColor(ProductResponse.ColorInfo.builder()
                        .id(product.getColor().getId())
                        .colorName(product.getColor().getColorName())
                        .hexCode(product.getColor().getHexCode())
                        .build())
                .primaryImage(primaryImage)
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductResponse mapToProductDetailResponse(Product product) {
        // Get all data for detailed response
        ProductResponse response = mapToProductResponse(product);

        // Add available colors
        List<Color> availableColors = productColorRepository.findColorsByProductId(product.getId());
        response.setAvailableColors(availableColors.stream()
                .map(color -> ProductResponse.ColorInfo.builder()
                        .id(color.getId())
                        .colorName(color.getColorName())
                        .hexCode(color.getHexCode())
                        .build())
                .collect(Collectors.toList()));

        // Add images
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDesc(product.getId());
        response.setImages(images.stream()
                .map(image -> ProductResponse.ImageInfo.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .altText(image.getAltText())
                        .isPrimary(image.getIsPrimary())
                        .build())
                .collect(Collectors.toList()));

        // Add specifications
        List<ProductSpecification> specs = productSpecificationRepository.findByProductId(product.getId());
        response.setSpecifications(specs.stream()
                .map(spec -> ProductResponse.SpecificationInfo.builder()
                        .id(spec.getId())
                        .specName(spec.getSpecName())
                        .specValue(spec.getSpecValue())
                        .build())
                .collect(Collectors.toList()));

        // Get reviews for this product
        // Note: Assuming ReviewRepository has findByProductId method
        // If not available, you can use: reviewRepository.findByProduct(product)
        List<Review> reviews = product.getReviews() != null ? product.getReviews() : new ArrayList<>();
        response.setReviews(reviews.stream()
                .limit(5) // Limit recent reviews
                .map(review -> ProductResponse.ReviewInfo.builder()
                        .id(review.getId())
                        .user(ProductResponse.UserInfo.builder()
                                .id(review.getUser().getId())
                                .fullName(review.getUser().getFullName())
                                .build())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList()));

        // Calculate average rating
        Double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        response.setAverageRating(averageRating);
        response.setTotalReviews(reviews.size());

        // Add category and brand descriptions
        response.getCategory().setDescription(product.getCategory().getDescription());
        response.getBrand().setDescription(product.getBrand().getDescription());

        return response;
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return switch (sortBy != null ? sortBy.toLowerCase() : "created_at") {
            case "price" -> Sort.by(direction, "price");
            case "name" -> Sort.by(direction, "name");
            case "created_at" -> Sort.by(direction, "createdAt");
            default -> Sort.by(direction, "createdAt");
        };
    }

    private boolean hasFilters(ProductFilterRequest filters) {
        return filters.getCategory() != null ||
                filters.getBrandId() != null ||
                filters.getColorId() != null ||
                filters.getMinPrice() != null ||
                filters.getMaxPrice() != null ||
                filters.getInStock() != null;
    }

    private Long getCategoryIdFromName(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return null;
        }
        // Note: Assuming CategoryRepository has findByName method
        // If not available, you may need to add this method to CategoryRepository
        return null; // Placeholder - implement based on your CategoryRepository
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
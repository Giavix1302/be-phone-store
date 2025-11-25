package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.category.CreateCategoryRequest;
import fit.se.be_phone_store.dto.request.category.UpdateCategoryRequest;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.dto.response.category.CategoryOverviewResponse;
import fit.se.be_phone_store.dto.response.category.CategoryResponse;
import fit.se.be_phone_store.dto.response.category.CategoryStatisticsResponse;
import fit.se.be_phone_store.entity.Category;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CategoryService - Business logic for Category operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories (User API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        log.info("Getting all categories for users");

        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        List<CategoryResponse> categoryResponses = categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Success getting all categories for users", categoryResponses);
    }

    /**
     * Get category detail (User API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<CategoryResponse> getCategoryDetail(Long id) {
        log.info("Getting category detail for ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        CategoryResponse response = mapToCategoryResponseWithDetails(category);

        return ApiResponse.success("Success", response);
    }

    /**
     * Get all categories (Admin API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<CategoryResponse>> getAllCategoriesAdmin(String sortBy, String sortOrder) {
        log.info("Getting all categories for admin - sortBy: {}, sortOrder: {}", sortBy, sortOrder);

        Sort sort = buildSort(sortBy, sortOrder);
        List<Category> categories = categoryRepository.findAll(sort);

        List<CategoryResponse> categoryResponses = categories.stream()
                .map(this::mapToAdminCategoryResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Success", categoryResponses);
    }

    /**
     * Create new category (Admin API)
     */
    public ApiResponse<CategoryResponse> createCategory(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Check if name already exists
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(request.getName().trim().toUpperCase());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with ID: {}", savedCategory.getId());

        CategoryResponse response = mapToCategoryResponse(savedCategory);

        return ApiResponse.success("Created success", response);
    }

    /**
     * Update category (Admin API)
     */
    public ApiResponse<CategoryResponse> updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category ID: {} with name: {}", id, request.getName());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        String newName = request.getName().trim().toUpperCase();

        // Check if new name already exists (excluding current category)
        if (!category.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        category.setName(newName);
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category ID: {}", updatedCategory.getId());

        CategoryResponse response = mapToAdminCategoryResponse(updatedCategory);

        return ApiResponse.success("Updated success", response);
    }

    /**
     * Delete category (Admin API)
     */
    public ApiResponse<Map<String, Object>> deleteCategory(Long id) {
        log.info("Deleting category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        // Check if category has products
        if (categoryRepository.hasProducts(id)) {
            throw new BadRequestException("Không thể xóa danh mục đang có sản phẩm. Vui lòng chuyển sản phẩm sang danh mục khác trước.");
        }

        categoryRepository.delete(category);
        log.info("Deleted category ID: {}", id);

        Map<String, Object> responseData = Map.of("deleted_category_id", id);

        return  ApiResponse.success("Deleted success", responseData);
    }

    /**
     * Get category statistics (Admin API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<CategoryStatisticsResponse> getCategoryStatistics(Long id) {
        log.info("Getting statistics for category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        // Get basic statistics
        List<Object[]> statisticsData = categoryRepository.getCategoryStatistics(id);

        // Get revenue
        Long totalOrders = categoryRepository.getTotalOrdersByCategory(id);

        // Get brand breakdown
        List<Object[]> brandData = categoryRepository.getBrandBreakdownByCategory(id);

        // Get price range
        List<Object[]> priceData = categoryRepository.getPriceRangeByCategory(id);

        CategoryStatisticsResponse response = buildCategoryStatisticsResponse(
                category, statisticsData, totalOrders, brandData, priceData);

        return ApiResponse.success("Getting statistics success", response);
    }

    /**
     * Get categories overview (Admin API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<CategoryOverviewResponse> getCategoriesOverview() {
        log.info("Getting categories overview");

        // Get basic overview data
        List<Object[]> overviewData = categoryRepository.getCategoriesOverview();

        // Calculate totals
        int totalCategories = (int) categoryRepository.count();
        int totalProducts = overviewData.stream()
                .mapToInt(data -> ((Long) data[2]).intValue())
                .sum();

        BigDecimal totalRevenue = overviewData.stream()
                .map(data -> (BigDecimal) data[3])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build category summaries
        List<CategoryOverviewResponse.CategorySummary> categoriesSummary = overviewData.stream()
                .map(data -> buildCategorySummary(data, totalRevenue))
                .collect(Collectors.toList());

        // Get trends
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> trendData = categoryRepository.getFastestGrowingCategories(thirtyDaysAgo);

        CategoryOverviewResponse.Trends trends = buildTrends(overviewData, trendData);

        CategoryOverviewResponse response = CategoryOverviewResponse.builder()
                .totalCategories(totalCategories)
                .totalProducts(totalProducts)
                .categoriesSummary(categoriesSummary)
                .trends(trends)
                .build();

        return ApiResponse.success("Getting categories overview success", response);
    }

    // Private helper methods

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProductCount())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private CategoryResponse mapToCategoryResponseWithDetails(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProductCount())
                .activeProductCount((int) category.getActiveProductCount())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private CategoryResponse mapToAdminCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProductCount())
                .activeProductCount((int) category.getActiveProductCount())
                .inactiveProductCount((int) category.getInactiveProductCount())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return switch (sortBy != null ? sortBy.toLowerCase() : "created_at") {
            case "name" -> Sort.by(direction, "name");
            case "product_count" -> Sort.by(direction, "products.size()");
            default -> Sort.by(direction, "createdAt");
        };
    }

    private CategoryStatisticsResponse buildCategoryStatisticsResponse(
            Category category, List<Object[]> statisticsData, Long totalOrders,
            List<Object[]> brandData, List<Object[]> priceData) {

        // Category info
        CategoryStatisticsResponse.CategoryInfo categoryInfo = CategoryStatisticsResponse.CategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();

        // Product statistics
        CategoryStatisticsResponse.ProductStatistics productStats = CategoryStatisticsResponse.ProductStatistics.builder()
                .totalProducts(category.getProductCount())
                .activeProducts((int) category.getActiveProductCount())
                .inactiveProducts((int) category.getInactiveProductCount())
                .productsInStock(0) // Will be calculated from actual data
                .productsOutOfStock(0) // Will be calculated from actual data
                .build();

        // Sales statistics
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal averagePrice = BigDecimal.ZERO;

        if (!priceData.isEmpty() && priceData.get(0)[2] != null) {
            averagePrice = (BigDecimal) priceData.get(0)[2];
        }

        CategoryStatisticsResponse.SalesStatistics salesStats = CategoryStatisticsResponse.SalesStatistics.builder()
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .totalRevenue(totalRevenue)
                .averageProductPrice(averagePrice)
                .build();

        // Brand breakdown
        List<CategoryStatisticsResponse.BrandBreakdown> brandBreakdown = brandData.stream()
                .map(data -> CategoryStatisticsResponse.BrandBreakdown.builder()
                        .brandId((Long) data[0])
                        .brandName((String) data[1])
                        .productCount(((Long) data[2]).intValue())
                        .revenue((BigDecimal) data[3])
                        .build())
                .collect(Collectors.toList());

        // Price range
        CategoryStatisticsResponse.PriceRange priceRange = CategoryStatisticsResponse.PriceRange.builder()
                .minPrice(priceData.isEmpty() ? BigDecimal.ZERO : (BigDecimal) priceData.get(0)[0])
                .maxPrice(priceData.isEmpty() ? BigDecimal.ZERO : (BigDecimal) priceData.get(0)[1])
                .averagePrice(priceData.isEmpty() ? BigDecimal.ZERO : (BigDecimal) priceData.get(0)[2])
                .build();

        return CategoryStatisticsResponse.builder()
                .category(categoryInfo)
                .productStatistics(productStats)
                .salesStatistics(salesStats)
                .brandBreakdown(brandBreakdown)
                .priceRange(priceRange)
                .build();
    }

    private CategoryOverviewResponse.CategorySummary buildCategorySummary(Object[] data, BigDecimal totalRevenue) {
        Long categoryId = (Long) data[0];
        String categoryName = (String) data[1];
        Integer productCount = ((Long) data[2]).intValue();
        BigDecimal revenue = (BigDecimal) data[3];

        Double revenuePercentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? revenue.multiply(BigDecimal.valueOf(100))
                .divide(totalRevenue, 2, RoundingMode.HALF_UP)
                .doubleValue()
                : 0.0;

        return CategoryOverviewResponse.CategorySummary.builder()
                .id(categoryId)
                .name(categoryName)
                .productCount(productCount)
                .revenuePercentage(revenuePercentage)
                .revenue(revenue)
                .build();
    }

    private CategoryOverviewResponse.Trends buildTrends(List<Object[]> overviewData, List<Object[]> trendData) {
        // Find most products
        CategoryOverviewResponse.MostProducts mostProducts = overviewData.stream()
                .max((a, b) -> ((Long) a[2]).compareTo((Long) b[2]))
                .map(data -> CategoryOverviewResponse.MostProducts.builder()
                        .categoryId((Long) data[0])
                        .categoryName((String) data[1])
                        .productCount(((Long) data[2]).intValue())
                        .build())
                .orElse(CategoryOverviewResponse.MostProducts.builder().build());

        // Find fastest growing
        CategoryOverviewResponse.FastestGrowing fastestGrowing = trendData.stream()
                .findFirst()
                .map(data -> CategoryOverviewResponse.FastestGrowing.builder()
                        .categoryId((Long) data[0])
                        .categoryName((String) data[1])
                        .growthRate(((Long) data[2]).doubleValue() * 2.5) // Mock calculation
                        .build())
                .orElse(CategoryOverviewResponse.FastestGrowing.builder().build());

        return CategoryOverviewResponse.Trends.builder()
                .mostProducts(mostProducts)
                .fastestGrowing(fastestGrowing)
                .build();
    }
}
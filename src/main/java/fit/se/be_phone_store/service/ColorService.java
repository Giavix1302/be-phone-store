package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.color.CreateColorRequest;
import fit.se.be_phone_store.dto.request.color.UpdateColorAdminRequest;
import fit.se.be_phone_store.dto.response.*;
import fit.se.be_phone_store.dto.response.color.ColorDetailResponse;
import fit.se.be_phone_store.dto.response.color.ColorResponse;
import fit.se.be_phone_store.dto.response.color.ColorStatisticsResponse;
import fit.se.be_phone_store.dto.response.color.ColorUsageResponse;
import fit.se.be_phone_store.entity.Color;
import fit.se.be_phone_store.entity.Product;
import fit.se.be_phone_store.entity.ProductColor;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.exception.ValidationException;
import fit.se.be_phone_store.exception.InvalidOperationException;
import fit.se.be_phone_store.repository.ColorRepository;
import fit.se.be_phone_store.repository.ProductRepository;
import fit.se.be_phone_store.repository.ProductColorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ColorService - Business logic for color management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ColorService {

    private final ColorRepository colorRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;

    private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    /**
     * Get all colors for users
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<ColorResponse>> getAllColors() {
        log.info("Getting all colors for user");

        List<Color> colors = colorRepository.findAllByOrderByColorNameAsc();
        List<ColorResponse> colorResponses = colors.stream()
                .map(this::convertToColorResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Lấy danh sách màu sắc thành công", colorResponses);
    }

    /**
     * Get color detail by ID
     */
    @Transactional(readOnly = true)
    public ApiResponse<ColorDetailResponse> getColorDetail(Long id) {
        log.info("Getting color detail for ID: {}", id);

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc không tồn tại"));

        ColorDetailResponse response = ColorDetailResponse.builder()
                .id(color.getId())
                .colorName(color.getColorName())
                .hexCode(color.getHexCode())
                .createdAt(LocalDateTime.now())
                .build();

        return ApiResponse.success("Lấy chi tiết màu sắc thành công", response);
    }

    /**
     * Get all colors for admin with pagination and search
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<ColorResponse> getAllColorsAdmin(
            int page, int limit, String search, String sortBy, String sortOrder) {

        log.info("Getting colors for admin - page: {}, limit: {}, search: {}", page, limit, search);

        // Validate sort parameters
        if (!isValidSortField(sortBy)) {
            sortBy = "colorName";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortBy));

        Page<Color> colorPage;
        if (search != null && !search.trim().isEmpty()) {
            // Simple search implementation - filter by color name
            List<Color> allColors = colorRepository.findAll(Sort.by(direction, sortBy));
            List<Color> filteredColors = allColors.stream()
                    .filter(color -> color.getColorName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredColors.size());
            List<Color> pageContent = filteredColors.subList(start, end);
            colorPage = new PageImpl<>(pageContent, pageable, filteredColors.size());
        } else {
            colorPage = colorRepository.findAll(pageable);
        }

        // Convert to Page<ColorResponse>
        List<ColorResponse> colorResponses = colorPage.getContent().stream()
                .map(this::convertToColorResponse)
                .collect(Collectors.toList());

        Page<ColorResponse> responsePage = new PageImpl<>(colorResponses, pageable, colorPage.getTotalElements());

        return PagedApiResponse.success("Lấy danh sách màu sắc thành công", responsePage);
    }

    /**
     * Create new color
     */
    public ApiResponse<ColorDetailResponse> createColor(CreateColorRequest request) {
        log.info("Creating new color: {}", request.getColorName());

        // Validate hex code format
        if (!isValidHexCode(request.getHexCode())) {
            throw new ValidationException("Mã màu hex không đúng định dạng. Vui lòng sử dụng format #RRGGBB");
        }

        // Normalize hex code to uppercase
        String normalizedHex = request.getHexCode().toUpperCase();

        Color color = new Color();
        color.setColorName(request.getColorName().trim());
        color.setHexCode(normalizedHex);

        Color savedColor = colorRepository.save(color);
        log.info("Created color with ID: {}", savedColor.getId());

        ColorDetailResponse response = ColorDetailResponse.builder()
                .id(savedColor.getId())
                .colorName(savedColor.getColorName())
                .hexCode(savedColor.getHexCode())
                .createdAt(LocalDateTime.now())
                .build();

        return ApiResponse.success("Tạo màu sắc thành công", response);
    }

    /**
     * Update color
     */
    public ApiResponse<ColorDetailResponse> updateColor(Long id, UpdateColorAdminRequest request) {
        log.info("Updating color ID: {}", id);

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc không tồn tại"));

        // Validate hex code format if provided
        if (request.getHexCode() != null && !isValidHexCode(request.getHexCode())) {
            throw new ValidationException("Mã màu hex không đúng định dạng. Vui lòng sử dụng format #RRGGBB");
        }

        // Update fields
        if (request.getColorName() != null) {
            color.setColorName(request.getColorName().trim());
        }
        if (request.getHexCode() != null) {
            color.setHexCode(request.getHexCode().toUpperCase());
        }

        Color savedColor = colorRepository.save(color);
        log.info("Updated color ID: {}", savedColor.getId());

        ColorDetailResponse response = ColorDetailResponse.builder()
                .id(savedColor.getId())
                .colorName(savedColor.getColorName())
                .hexCode(savedColor.getHexCode())
                .updatedAt(LocalDateTime.now())
                .build();

        return ApiResponse.success("Cập nhật màu sắc thành công", response);
    }

    /**
     * Delete color
     */
    public ApiResponse<Map<String, Object>> deleteColor(Long id) {
        log.info("Deleting color ID: {}", id);

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc không tồn tại"));

        // Check if color is being used by products
        List<Product> productsWithColor = color.getProducts();
        List<ProductColor> productColorsWithColor = color.getProductColors();

        if ((productsWithColor != null && !productsWithColor.isEmpty()) ||
                (productColorsWithColor != null && !productColorsWithColor.isEmpty())) {
            throw new InvalidOperationException("Không thể xóa màu sắc đang được sử dụng bởi sản phẩm");
        }

        colorRepository.delete(color);
        log.info("Deleted color ID: {}", id);

        Map<String, Object> result = new HashMap<>();
        result.put("deleted_color_id", id);

        return ApiResponse.success("Xóa màu sắc thành công", result);
    }

    /**
     * Get color usage information
     */
    @Transactional(readOnly = true)
    public ApiResponse<ColorUsageResponse> getColorUsage(Long id) {
        log.info("Getting color usage for ID: {}", id);

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc không tồn tại"));

        // Get products using this color as default
        List<Product> defaultProducts = color.getProducts() != null ? color.getProducts() : List.of();
        List<ProductColor> variantProducts = color.getProductColors() != null ? color.getProductColors() : List.of();

        // Build usage summary
        ColorUsageResponse.UsageSummary summary = ColorUsageResponse.UsageSummary.builder()
                .totalProducts((long) (defaultProducts.size() + variantProducts.size()))
                .asDefaultColor((long) defaultProducts.size())
                .asAvailableColor((long) variantProducts.size())
                .totalUsage((long) (defaultProducts.size() + variantProducts.size()))
                .build();

        // Convert to simple product info
        List<ColorUsageResponse.ProductInfo> defaultProductInfos = defaultProducts.stream()
                .map(p -> ColorUsageResponse.ProductInfo.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .slug(p.getSlug())
                        .build())
                .collect(Collectors.toList());

        List<ColorUsageResponse.ProductInfo> variantProductInfos = variantProducts.stream()
                .map(pc -> ColorUsageResponse.ProductInfo.builder()
                        .id(pc.getProduct().getId())
                        .name(pc.getProduct().getName())
                        .slug(pc.getProduct().getSlug())
                        .build())
                .collect(Collectors.toList());

        boolean canDelete = summary.getTotalUsage() == 0;
        String deleteBlockedReason = canDelete ? null :
                String.format("Màu sắc đang được sử dụng làm màu mặc định cho %d sản phẩm",
                        summary.getAsDefaultColor());

        ColorUsageResponse response = ColorUsageResponse.builder()
                .color(convertToColorResponse(color))
                .usageSummary(summary)
                .productsUsingAsDefault(defaultProductInfos)
                .productsUsingAsAvailable(variantProductInfos)
                .canDelete(canDelete)
                .deleteBlockedReason(deleteBlockedReason)
                .build();

        return ApiResponse.success("Lấy thông tin sử dụng màu sắc thành công", response);
    }

    /**
     * Get color statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<ColorStatisticsResponse> getColorStatistics() {
        log.info("Getting color statistics");

        long totalColors = colorRepository.count();

        // Get colors used in products (simplified approach)
        List<Color> allColors = colorRepository.findAll();
        List<Color> usedColors = allColors.stream()
                .filter(color -> (color.getProducts() != null && !color.getProducts().isEmpty()) ||
                        (color.getProductColors() != null && !color.getProductColors().isEmpty()))
                .collect(Collectors.toList());

        long colorsInUse = usedColors.size();
        long colorsUnused = totalColors - colorsInUse;

        // Get most used colors
        List<ColorStatisticsResponse.ColorUsage> mostUsedColors = usedColors.stream()
                .map(color -> {
                    int productCount = (color.getProducts() != null ? color.getProducts().size() : 0) +
                            (color.getProductColors() != null ? color.getProductColors().size() : 0);
                    return ColorStatisticsResponse.ColorUsage.builder()
                            .id(color.getId())
                            .colorName(color.getColorName())
                            .hexCode(color.getHexCode())
                            .productCount((long) productCount)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getProductCount(), a.getProductCount()))
                .limit(5)
                .collect(Collectors.toList());

        // Get unused colors
        List<ColorResponse> unusedColors = allColors.stream()
                .filter(c -> !usedColors.contains(c))
                .map(this::convertToColorResponse)
                .collect(Collectors.toList());

        // Build statistics response
        ColorStatisticsResponse.Overview overview = ColorStatisticsResponse.Overview.builder()
                .totalColors(totalColors)
                .colorsInUse(colorsInUse)
                .colorsUnused(colorsUnused)
                .mostPopularCount(mostUsedColors.isEmpty() ? 0L : mostUsedColors.get(0).getProductCount())
                .build();

        ColorStatisticsResponse response = ColorStatisticsResponse.builder()
                .overview(overview)
                .mostUsedColors(mostUsedColors)
                .unusedColors(unusedColors)
                .build();

        return ApiResponse.success("Lấy thống kê màu sắc thành công", response);
    }

    // Helper methods
    private ColorResponse convertToColorResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId())
                .colorName(color.getColorName())
                .hexCode(color.getHexCode())
                .build();
    }

    private boolean isValidHexCode(String hexCode) {
        return hexCode != null && HEX_PATTERN.matcher(hexCode).matches();
    }

    private boolean isValidSortField(String sortBy) {
        return "colorName".equals(sortBy) || "createdAt".equals(sortBy);
    }
}
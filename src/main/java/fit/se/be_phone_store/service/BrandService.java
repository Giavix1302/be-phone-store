package fit.se.be_phone_store.service;

import fit.se.be_phone_store.dto.request.brand.CreateBrandRequest;
import fit.se.be_phone_store.dto.request.brand.UpdateBrandRequest;
import fit.se.be_phone_store.dto.response.ApiResponse;
import fit.se.be_phone_store.dto.response.PagedApiResponse;
import fit.se.be_phone_store.dto.response.brand.BrandResponse;
import fit.se.be_phone_store.entity.Brand;
import fit.se.be_phone_store.exception.BadRequestException;
import fit.se.be_phone_store.exception.ResourceNotFoundException;
import fit.se.be_phone_store.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BrandService - Business logic for Brand operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrandService {

    private final BrandRepository brandRepository;

    /**
     * Get all brands (User API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<BrandResponse>> getAllBrands() {
        log.info("Getting all brands for users");

        List<Brand> brands = brandRepository.findAllByOrderByNameAsc();

        List<BrandResponse> brandResponses = brands.stream()
                .map(this::mapToBrandResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Lấy danh sách thương hiệu thành công", brandResponses);
    }

    /**
     * Get brand detail (User API)
     */
    @Transactional(readOnly = true)
    public ApiResponse<BrandResponse> getBrandDetail(Long id) {
        log.info("Getting brand detail for ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));

        BrandResponse response = mapToBrandResponseWithDetails(brand);

        return ApiResponse.success("Lấy chi tiết thương hiệu thành công", response);
    }

    /**
     * Get all brands with pagination and search (Admin API)
     */
    @Transactional(readOnly = true)
    public PagedApiResponse<BrandResponse> getAllBrandsAdmin(
            int page, int limit, String search, String sortBy, String sortOrder) {

        log.info("Getting brands for admin - page: {}, limit: {}, search: {}, sortBy: {}, sortOrder: {}",
                page, limit, search, sortBy, sortOrder);

        // Build pageable with sort
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page - 1, limit, sort); // Convert 1-based to 0-based

        List<Brand> brands;
        long totalElements;

        if (search != null && !search.trim().isEmpty()) {
            brands = brandRepository.searchByName(search.trim());
            totalElements = brands.size();

            // Manual pagination for search results
            int start = Math.min((page - 1) * limit, brands.size());
            int end = Math.min(start + limit, brands.size());
            brands = brands.subList(start, end);
        } else {
            brands = brandRepository.findAll(sort);
            totalElements = brands.size();

            // Manual pagination for all results
            int start = Math.min((page - 1) * limit, brands.size());
            int end = Math.min(start + limit, brands.size());
            brands = brands.subList(start, end);
        }

        List<BrandResponse> brandResponses = brands.stream()
                .map(this::mapToAdminBrandResponse)
                .collect(Collectors.toList());

        // Calculate pagination info
        int totalPages = (int) Math.ceil((double) totalElements / limit);
        boolean hasNext = page < totalPages;
        boolean hasPrevious = page > 1;
        boolean first = page == 1;
        boolean last = page >= totalPages;

        PagedApiResponse.PaginationInfo pagination = PagedApiResponse.PaginationInfo.builder()
                .currentPage(page)
                .pageSize(limit)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(first)
                .last(last)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();

        return PagedApiResponse.success("Lấy danh sách thương hiệu thành công", brandResponses, pagination);
    }

    /**
     * Create new brand (Admin API)
     */
    public ApiResponse<BrandResponse> createBrand(CreateBrandRequest request) {
        log.info("Creating new brand: {}", request.getName());

        String brandName = request.getName().trim();

        // Check if name already exists
        if (brandRepository.existsByName(brandName)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        Brand brand = new Brand();
        brand.setName(brandName);
        brand.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand with ID: {}", savedBrand.getId());

        BrandResponse response = mapToBrandResponse(savedBrand);

        return ApiResponse.success("Tạo thương hiệu thành công", response);
    }

    /**
     * Update brand (Admin API)
     */
    public ApiResponse<BrandResponse> updateBrand(Long id, UpdateBrandRequest request) {
        log.info("Updating brand ID: {} with name: {}", id, request.getName());

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));

        String newName = request.getName().trim();

        // Check if new name already exists (excluding current brand)
        if (!brand.getName().equalsIgnoreCase(newName) && brandRepository.existsByName(newName)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        brand.setName(newName);
        brand.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Brand updatedBrand = brandRepository.save(brand);
        log.info("Updated brand ID: {}", updatedBrand.getId());

        BrandResponse response = mapToAdminBrandResponse(updatedBrand);

        return ApiResponse.success("Cập nhật thương hiệu thành công", response);
    }

    /**
     * Delete brand (Admin API)
     */
    public ApiResponse<Map<String, Object>> deleteBrand(Long id) {
        log.info("Deleting brand ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));

        // Check if brand has products
        if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
            throw new BadRequestException("Không thể xóa thương hiệu đang có sản phẩm. Vui lòng chuyển sản phẩm sang thương hiệu khác trước.");
        }

        brandRepository.delete(brand);
        log.info("Deleted brand ID: {}", id);

        Map<String, Object> responseData = Map.of("deleted_brand_id", id);

        return ApiResponse.success("Xóa thương hiệu thành công", responseData);
    }

    // Private helper methods

    private BrandResponse mapToBrandResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .createdAt(brand.getCreatedAt())
                .build();
    }

    private BrandResponse mapToBrandResponseWithDetails(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .productCount(brand.getProducts() != null ? brand.getProducts().size() : 0)
                .createdAt(brand.getCreatedAt())
                .build();
    }

    private BrandResponse mapToAdminBrandResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .productCount(brand.getProducts() != null ? brand.getProducts().size() : 0)
                .createdAt(brand.getCreatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "name" -> Sort.by(direction, "name");
            case "created_at" -> Sort.by(direction, "createdAt");
            default -> Sort.by(direction, "name");
        };
    }
}
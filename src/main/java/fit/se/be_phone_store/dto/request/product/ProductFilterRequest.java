package fit.se.be_phone_store.dto.request.product;

import java.math.BigDecimal;

/**
 * ProductFilterRequest DTO for product search and filtering
 * WITHOUT Lombok - Manual getters and setters
 */
public class ProductFilterRequest {

    private String search;
    private String category;
    private Long brandId;
    private Long colorId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;
    private Boolean isActive;
    private String sortBy = "created_at";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer limit = 10;

    // Constructors
    public ProductFilterRequest() {
    }

    public ProductFilterRequest(String search, String category, Long brandId, Long colorId,
                                BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
                                Boolean isActive, String sortBy, String sortOrder,
                                Integer page, Integer limit) {
        this.search = search;
        this.category = category;
        this.brandId = brandId;
        this.colorId = colorId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.inStock = inStock;
        this.isActive = isActive;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.page = page;
        this.limit = limit;
    }

    // Getters and Setters
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "ProductFilterRequest{" +
                "search='" + search + '\'' +
                ", category='" + category + '\'' +
                ", brandId=" + brandId +
                ", colorId=" + colorId +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", inStock=" + inStock +
                ", isActive=" + isActive +
                ", sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", page=" + page +
                ", limit=" + limit +
                '}';
    }
}
package fit.se.be_phone_store.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CreateProductRequest DTO for creating new products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    @Digits(integer = 8, fraction = 2, message = "Giá sản phẩm không hợp lệ")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá khuyến mãi phải lớn hơn 0")
    @Digits(integer = 8, fraction = 2, message = "Giá khuyến mãi không hợp lệ")
    private BigDecimal discountPrice;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity = 0;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Thương hiệu không được để trống")
    private Long brandId;

    @NotNull(message = "Màu mặc định không được để trống")
    private Long colorId;

    @NotEmpty(message = "Phải có ít nhất một màu khả dụng")
    private List<Long> colorIds;

    private Boolean isActive = true;

    // For multipart form data
    private List<String> imageAlts; // Alt text for images
    private Integer primaryImageIndex = 0;

    // Validation method
    @AssertTrue(message = "Giá khuyến mãi phải nhỏ hơn giá gốc")
    public boolean isDiscountValid() {
        if (discountPrice == null) return true;
        return discountPrice.compareTo(price) < 0;
    }

    @AssertTrue(message = "Màu mặc định phải có trong danh sách màu khả dụng")
    public boolean isDefaultColorValid() {
        if (colorIds == null || colorIds.isEmpty()) return true;
        return colorIds.contains(colorId);
    }
}
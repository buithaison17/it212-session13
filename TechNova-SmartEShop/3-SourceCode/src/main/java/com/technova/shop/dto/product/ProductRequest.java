package com.technova.shop.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO phục vụ cho CRUD sản phẩm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được trống")
    private String name;

    private String description;

    @NotNull(message = "Giá tiền không được trống")
    @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được trống")
    @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private Integer stockQuantity;

    private String category;
    private String imageUrl;
}

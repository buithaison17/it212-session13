package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.product.ProductRequest;
import com.technova.shop.dto.product.ProductResponse;
import com.technova.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các thao tác quản trị sản phẩm dành cho Staff và Manager.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Product Admin Module", description = "Các API quản lý sản phẩm (Yêu cầu quyền STAFF hoặc MANAGER)")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Thêm mới sản phẩm (Staff & Manager)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.createProduct(productRequest);
        return new ResponseEntity<>(ApiResponse.success(response, "Thêm sản phẩm thành công"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa thông tin sản phẩm bằng ID (Staff & Manager)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật sản phẩm thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ẩn/Xóa mềm sản phẩm bằng ID (Staff & Manager)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công (Ẩn sản phẩm)"));
    }
}

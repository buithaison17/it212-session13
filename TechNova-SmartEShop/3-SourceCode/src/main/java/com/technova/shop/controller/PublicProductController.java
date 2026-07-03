package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.product.ProductResponse;
import com.technova.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API xem sản phẩm công khai dành cho tất cả mọi người (kể cả Guest).
 */
@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@Tag(name = "Product Public Module", description = "Các API duyệt sản phẩm công khai (Không cần đăng nhập)")
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm đang bán (phân trang)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<ProductResponse> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách sản phẩm thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một sản phẩm bằng ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getActiveProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy chi tiết sản phẩm thành công"));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm theo tên hoặc danh mục (phân trang)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam("q") String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<ProductResponse> products = productService.searchActiveProducts(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(products, "Tìm kiếm sản phẩm thành công"));
    }
}

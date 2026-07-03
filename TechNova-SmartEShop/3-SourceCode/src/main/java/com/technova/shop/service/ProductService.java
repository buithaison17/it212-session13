package com.technova.shop.service;

import com.technova.shop.dto.product.ProductRequest;
import com.technova.shop.dto.product.ProductResponse;
import com.technova.shop.entity.Product;
import com.technova.shop.exception.ResourceNotFoundException;
import com.technova.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý các logic nghiệp vụ liên quan đến quản lý sản phẩm.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Lấy danh sách sản phẩm đang active (công khai), hỗ trợ phân trang.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAllByActiveTrue(pageable);
        return products.map(this::mapToResponse);
    }

    /**
     * Lấy chi tiết sản phẩm active bằng id.
     */
    @Transactional(readOnly = true)
    public ProductResponse getActiveProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm hoặc sản phẩm đã ngừng kinh doanh."));
        return mapToResponse(product);
    }

    /**
     * Tìm kiếm sản phẩm active theo tên hoặc danh mục, hỗ trợ phân trang.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchActiveProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseAndActiveTrue(
                keyword, keyword, pageable);
        return products.map(this::mapToResponse);
    }

    /**
     * Admin: Thêm sản phẩm mới.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    /**
     * Admin: Sửa thông tin sản phẩm.
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm để cập nhật với ID: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    /**
     * Admin: Xóa sản phẩm (Soft Delete).
     * Thiết lập active = false để không ảnh hưởng đến đơn hàng cũ đã đặt.
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm để xóa với ID: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    // Helper map Entity -> DTO
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

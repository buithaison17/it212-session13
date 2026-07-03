package com.technova.shop.repository;

import com.technova.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho thực thể Product.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Danh sách sản phẩm đang mở bán (active = true), hỗ trợ phân trang
    Page<Product> findAllByActiveTrue(Pageable pageable);

    // Xem chi tiết sản phẩm đang mở bán
    Optional<Product> findByIdAndActiveTrue(Long id);

    // Tìm kiếm sản phẩm theo từ khóa (trong tên hoặc danh mục) và đang active, hỗ trợ phân trang
    Page<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseAndActiveTrue(
            String nameKeyword, String categoryKeyword, Pageable pageable);
}

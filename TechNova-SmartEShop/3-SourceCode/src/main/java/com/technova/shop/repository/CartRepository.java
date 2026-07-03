package com.technova.shop.repository;

import com.technova.shop.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho thực thể Cart.
 * Sử dụng EntityGraph để giải quyết N+1 query khi load Cart kèm CartItems và Products.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    /**
     * Lấy giỏ hàng của người dùng, nạp đồng thời toàn bộ CartItems và thực thể Product đi kèm.
     * Tránh việc phát sinh 1 query lấy Cart, 1 query lấy CartItems và N query lấy Product.
     *
     * @param userId ID người dùng
     * @return Optional chứa Cart đầy đủ chi tiết
     */
    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findWithItemsByUserId(Long userId);
}

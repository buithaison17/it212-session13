package com.technova.shop.repository;

import com.technova.shop.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho thực thể OrderItem.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Truy vấn top sản phẩm bán chạy nhất trong khoảng thời gian xác định (loại trừ các đơn hàng bị HỦY).
     *
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @param pageable đối tượng phân trang để giới hạn số lượng kết quả (ví dụ: lấy top 5)
     * @return List mảng Object chứa [ProductId, ProductName, TotalQuantity, TotalRevenue]
     */
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity), SUM(oi.quantity * oi.unitPrice) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.orderedAt BETWEEN :startDate AND :endDate " +
           "AND oi.order.status != com.technova.shop.entity.OrderStatus.CANCELLED " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}

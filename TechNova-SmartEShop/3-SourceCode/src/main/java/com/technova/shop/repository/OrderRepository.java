package com.technova.shop.repository;

import com.technova.shop.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho thực thể Order.
 * Định cấu hình các câu truy vấn kèm EntityGraph để triệt tiêu N+1 query.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Lấy lịch sử đơn hàng của một người dùng, nạp sẵn danh sách sản phẩm và chi tiết để tránh N+1.
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findAllByUserIdOrderByOrderedAtDesc(Long userId);

    /**
     * Lấy danh sách tất cả đơn hàng cho Admin, nạp kèm User đặt hàng, OrderItems và Product.
     */
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o ORDER BY o.orderedAt DESC")
    List<Order> findAllWithUserAndItemsOrderByOrderedAtDesc();

    /**
     * Tính tổng doanh thu trong khoảng thời gian (loại trừ các đơn hàng bị HỦY - CANCELLED).
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
           "WHERE o.orderedAt BETWEEN :startDate AND :endDate " +
           "AND o.status != com.technova.shop.entity.OrderStatus.CANCELLED")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Thống kê số lượng đơn hàng theo từng trạng thái.
     * Trả về List các mảng Object [Trạng thái (String/Enum), Số lượng (Long)]
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
           "WHERE o.orderedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.status")
    List<Object[]> countOrdersByStatus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

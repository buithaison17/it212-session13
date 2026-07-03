package com.technova.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho bảng ORDERS trong cơ sở dữ liệu.
 * Chứa thông tin đơn hàng đã thực hiện bởi khách hàng.
 *
 * <h2>Giải quyết bài toán N+1 Query:</h2>
 * Mối quan hệ giữa Order và OrderItem là One-to-Many.
 * Bắt buộc cấu hình {@link FetchType#LAZY} ở annotation {@link OneToMany}.
 *
 * <b>Tại sao xảy ra N+1?</b>
 * Khi lấy danh sách N đơn hàng, nếu fetch type là EAGER, JPA sẽ tự động thực hiện
 * thêm N câu lệnh select phụ để lấy thông tin các OrderItem đi kèm, dẫn đến N+1 queries.
 *
 * <b>Cách giải quyết:</b>
 * 1. Đặt {@link FetchType#LAZY} làm mặc định.
 * 2. Khi cần hiển thị thông tin chi tiết đơn hàng (kèm sản phẩm), sử dụng `@EntityGraph`
 * trong Repository để nạp trước (fetch join) cả {@code orderItems} và {@code product}
 * liên quan trong 1 câu SQL duy nhất.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod = "COD"; // Mặc định là COD thanh toán khi nhận hàng

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

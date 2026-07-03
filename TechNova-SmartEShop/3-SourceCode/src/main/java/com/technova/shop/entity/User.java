package com.technova.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Đại diện cho bảng USERS trong cơ sở dữ liệu.
 * Chứa thông tin tài khoản người dùng của TechNova Smart E-Shop.
 *
 * <h2>Giải quyết bài toán N+1 Query:</h2>
 * Mối quan hệ giữa User và Role là Many-to-Many.
 * Bắt buộc cấu hình {@link FetchType#LAZY} ở annotation {@link ManyToMany}.
 *
 * <b>Tại sao xảy ra N+1?</b>
 * Nếu cấu hình là {@link FetchType#EAGER}, mỗi khi tải danh sách N người dùng,
 * JPA sẽ chạy thêm N câu truy vấn để lấy Role cho từng người dùng riêng lẻ -> tổng cộng 1 + N queries.
 *
 * <b>Cách giải quyết:</b>
 * 1. Sử dụng {@link FetchType#LAZY} để mặc định không tự động tải dữ liệu Roles liên quan.
 * 2. Khi thực sự cần lấy thông tin User kèm Roles (ví dụ trong luồng xác thực Spring Security),
 * ta sẽ sử dụng {@link org.springframework.data.jpa.repository.EntityGraph} trong Repository
 * để chỉ định lấy nạp đồng thời (Fetch Join) chỉ trong đúng câu truy vấn đó.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

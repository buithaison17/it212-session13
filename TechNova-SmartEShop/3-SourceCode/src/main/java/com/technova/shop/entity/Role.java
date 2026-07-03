package com.technova.shop.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Đại diện cho bảng ROLES trong cơ sở dữ liệu.
 * Chứa các vai trò bảo mật của hệ thống: ROLE_CUSTOMER, ROLE_STAFF, ROLE_MANAGER.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, unique = true, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;
}

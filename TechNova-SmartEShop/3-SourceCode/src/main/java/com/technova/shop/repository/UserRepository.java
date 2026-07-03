package com.technova.shop.repository;

import com.technova.shop.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho thực thể User.
 * Có cấu hình EntityGraph để tránh N+1 Query khi tải User kèm Roles.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * Tìm người dùng bằng username và nạp đồng thời danh sách roles.
     * Sử dụng @EntityGraph để tránh N+1 query (phát sinh thêm query lấy roles sau query lấy user).
     *
     * @param username tên đăng nhập
     * @return Optional chứa User kèm Roles đã được nạp
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

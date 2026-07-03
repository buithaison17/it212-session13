package com.technova.shop.service;

import com.technova.shop.dto.auth.UserResponse;
import com.technova.shop.entity.Role;
import com.technova.shop.entity.User;
import com.technova.shop.exception.BadRequestException;
import com.technova.shop.exception.ResourceNotFoundException;
import com.technova.shop.repository.RoleRepository;
import com.technova.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Xử lý các logic nghiệp vụ liên quan đến quản lý người dùng và phân quyền nhân viên.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Manager: Lấy danh sách toàn bộ người dùng trong hệ thống.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Manager: Gán hoặc thu hồi vai trò (Roles) của một nhân viên.
     * RÀNG BUỘC: Không được tự gán/thu hồi role cho chính bản thân.
     */
    @Transactional
    public UserResponse updateUserRoles(Long id, List<String> roleNames, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Ràng buộc bảo mật: Không được tự thay đổi role của chính mình
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new BadRequestException("Cảnh báo bảo mật: Bạn không được tự thay đổi quyền hạn (Role) của chính mình.");
        }

        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            // Chuẩn hóa role name (phải bắt đầu bằng ROLE_ nếu client gửi thiếu)
            String normalizedName = name.startsWith("ROLE_") ? name : "ROLE_" + name;
            Role role = roleRepository.findByName(normalizedName)
                    .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại trong hệ thống: " + name));
            roles.add(role);
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    // Helper map Entity -> DTO
    private UserResponse mapToResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .enabled(user.isEnabled())
                .roles(roles)
                .build();
    }
}

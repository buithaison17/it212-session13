package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.auth.UserResponse;
import com.technova.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý tài khoản người dùng nội bộ và phân quyền (chỉ dành cho Manager).
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Admin Module", description = "Các API quản lý tài khoản & phân quyền (Yêu cầu quyền MANAGER)")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả tài khoản người dùng trong hệ thống (Manager)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách tài khoản thành công"));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Phân quyền / gán quyền hạn cho tài khoản nhân viên (Manager)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long id,
            @RequestBody List<String> roleNames,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        UserResponse response = userService.updateUserRoles(id, roleNames, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật quyền hạn thành công"));
    }
}

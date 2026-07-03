package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.order.OrderResponse;
import com.technova.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller xử lý quản lý đơn hàng dành cho Admin (Manager).
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Order Admin Module", description = "Các API quản lý đơn hàng cho Admin (Yêu cầu quyền MANAGER)")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Lấy tất cả danh sách các đơn hàng trong hệ thống (Manager)")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders, "Lấy danh sách tất cả đơn hàng thành công"));
    }
}

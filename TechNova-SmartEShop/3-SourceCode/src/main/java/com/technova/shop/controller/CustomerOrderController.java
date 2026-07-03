package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.order.OrderResponse;
import com.technova.shop.dto.order.PlaceOrderRequest;
import com.technova.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API đặt hàng và xem lịch sử mua hàng của khách hàng.
 */
@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Order Customer Module", description = "Các API đặt hàng dành cho khách hàng (Yêu cầu đăng nhập)")
public class CustomerOrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Tiến hành checkout đặt đơn hàng mới")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request) {
        String username = authentication.getName();
        OrderResponse order = orderService.placeOrder(username, request);
        return new ResponseEntity<>(ApiResponse.success(order, "Đặt hàng thành công"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Xem lịch sử đơn hàng cá nhân")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        String username = authentication.getName();
        List<OrderResponse> orders = orderService.getMyOrders(username);
        return ResponseEntity.ok(ApiResponse.success(orders, "Lấy danh sách đơn hàng thành công"));
    }
}

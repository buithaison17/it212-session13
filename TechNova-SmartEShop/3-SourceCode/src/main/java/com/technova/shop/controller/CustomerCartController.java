package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.cart.AddToCartRequest;
import com.technova.shop.dto.cart.CartResponse;
import com.technova.shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API giỏ hàng dành cho khách hàng.
 */
@RestController
@RequestMapping("/api/v1/customer/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Cart Module", description = "Các API giỏ hàng dành cho khách hàng (Yêu cầu đăng nhập)")
public class CustomerCartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng hiện tại")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        String username = authentication.getName();
        CartResponse cart = cartService.getCartOfUser(username);
        return ResponseEntity.ok(ApiResponse.success(cart, "Lấy giỏ hàng thành công"));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        String username = authentication.getName();
        CartResponse cart = cartService.addItemToCart(username, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Thêm sản phẩm vào giỏ hàng thành công"));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng bằng ID dòng (CartItem ID)")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            Authentication authentication,
            @PathVariable("id") Long itemId) {
        String username = authentication.getName();
        CartResponse cart = cartService.removeItemFromCart(username, itemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Xóa sản phẩm khỏi giỏ hàng thành công"));
    }
}

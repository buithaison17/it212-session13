package com.technova.shop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO thông tin giỏ hàng của người dùng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long id;
    private List<CartItemResponse> cartItems;
    private BigDecimal totalAmount;
}

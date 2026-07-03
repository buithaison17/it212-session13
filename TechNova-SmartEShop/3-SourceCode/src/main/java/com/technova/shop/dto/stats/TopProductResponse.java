package com.technova.shop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin sản phẩm bán chạy nhất phục vụ báo cáo thống kê.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductResponse {
    private Long productId;
    private String productName;
    private Long quantitySold;
    private BigDecimal revenueGenerated;
}

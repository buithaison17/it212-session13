package com.technova.shop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO phản hồi báo cáo thống kê doanh thu và hoạt động bán hàng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueStatsResponse {
    private BigDecimal totalRevenue;
    private Map<String, Long> orderStatusCounts;
    private List<TopProductResponse> topSellingProducts;
}

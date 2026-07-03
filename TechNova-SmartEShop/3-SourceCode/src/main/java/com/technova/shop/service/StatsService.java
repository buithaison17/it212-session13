package com.technova.shop.service;

import com.technova.shop.dto.stats.RevenueStatsResponse;
import com.technova.shop.dto.stats.TopProductResponse;
import com.technova.shop.repository.OrderItemRepository;
import com.technova.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý các nghiệp vụ thống kê doanh thu và báo cáo bán hàng dành cho quản lý.
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Thống kê doanh thu, số lượng đơn hàng theo trạng thái, và top 5 sản phẩm bán chạy nhất trong khoảng thời gian.
     */
    @Transactional(readOnly = true)
    public RevenueStatsResponse getRevenueStats(LocalDate startDate, LocalDate endDate) {
        // Chuyển đổi LocalDate sang LocalDateTime bao quát trọn vẹn từ 00:00:00 ngày bắt đầu đến 23:59:59.999 ngày kết thúc
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 1. Tính tổng doanh thu
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(startDateTime, endDateTime);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 2. Thống kê số đơn hàng theo trạng thái
        List<Object[]> statusCountsRaw = orderRepository.countOrdersByStatus(startDateTime, endDateTime);
        Map<String, Long> orderStatusCounts = new HashMap<>();
        for (Object[] row : statusCountsRaw) {
            if (row[0] != null) {
                String statusStr = row[0].toString();
                Long count = (Long) row[1];
                orderStatusCounts.put(statusStr, count);
            }
        }

        // 3. Lấy danh sách Top 5 sản phẩm bán chạy nhất
        List<Object[]> topProductsRaw = orderItemRepository.findTopSellingProducts(
                startDateTime, endDateTime, PageRequest.of(0, 5));
        List<TopProductResponse> topSellingProducts = new ArrayList<>();

        for (Object[] row : topProductsRaw) {
            Long productId = (Long) row[0];
            String productName = (String) row[1];
            Long quantitySold = (Long) row[2];
            BigDecimal revenueGenerated = (BigDecimal) row[3];

            topSellingProducts.add(TopProductResponse.builder()
                    .productId(productId)
                    .productName(productName)
                    .quantitySold(quantitySold)
                    .revenueGenerated(revenueGenerated)
                    .build());
        }

        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .orderStatusCounts(orderStatusCounts)
                .topSellingProducts(topSellingProducts)
                .build();
    }
}

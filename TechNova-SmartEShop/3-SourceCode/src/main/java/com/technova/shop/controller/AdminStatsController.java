package com.technova.shop.controller;

import com.technova.shop.dto.ApiResponse;
import com.technova.shop.dto.stats.RevenueStatsResponse;
import com.technova.shop.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controller xử lý các yêu cầu thống kê báo cáo (chỉ dành cho Manager).
 */
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Statistics Admin Module", description = "Các API thống kê và báo cáo kinh doanh (Yêu cầu quyền MANAGER)")
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/revenue")
    @Operation(summary = "Thống kê doanh thu, trạng thái đơn hàng và top sản phẩm trong khoảng thời gian")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        RevenueStatsResponse response = statsService.getRevenueStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy dữ liệu thống kê doanh thu thành công"));
    }
}

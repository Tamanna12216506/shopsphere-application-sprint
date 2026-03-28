package com.capgemini.adminservice.service.impl;

import com.capgemini.adminservice.client.OrderClient;
import com.capgemini.adminservice.dto.*;
import com.capgemini.adminservice.enums.OrderStatus; // ✅ import enum
import com.capgemini.adminservice.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final OrderClient orderClient;

    @Override
    public RevenueReportDTO getRevenueReport() {

        // Fetch all orders from Order Service
        List<OrderDTO> orders = orderClient.getAllOrders().getData();
        // ✅ Total revenue (excluding CANCELLED & FAILED)
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED
                        && o.getOrderStatus() != OrderStatus.FAILED)
                .map(OrderDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ Total valid orders count
        long totalOrders = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED
                        && o.getOrderStatus() != OrderStatus.FAILED)
                .count();

        // ✅ Average order value
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(
                BigDecimal.valueOf(totalOrders),
                2,
                RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ✅ Revenue grouped by status (ENUM → STRING)
        Map<String, BigDecimal> revenueByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderStatus().name(), // convert enum to String
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                OrderDTO::getTotalAmount,
                                BigDecimal::add)
                ));

        // ✅ Order count grouped by status (ENUM → STRING)
        Map<String, Long> countByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderStatus().name(), // convert enum to String
                        Collectors.counting()
                ));

        // ✅ Build and return response
        return RevenueReportDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .revenueByStatus(revenueByStatus)
                .orderCountByStatus(countByStatus)
                .build();
    }
}
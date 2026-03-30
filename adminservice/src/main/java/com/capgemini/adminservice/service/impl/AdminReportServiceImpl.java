package com.capgemini.adminservice.service.impl;

import com.capgemini.adminservice.client.OrderClient;
import com.capgemini.adminservice.dto.OrderDTO;
import com.capgemini.adminservice.dto.RevenueReportDTO;
import com.capgemini.adminservice.enums.OrderStatus;
import com.capgemini.adminservice.service.AdminReportService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    // Opens the circuit if report calls keep failing.
    @CircuitBreaker(name = "orderService", fallbackMethod = "getRevenueReportFallback")
    // Retries brief report call issues before failing the request.
    @Retry(name = "orderService")
    @Override
    public RevenueReportDTO getRevenueReport() {

        // Fetch all orders from Order Service
        List<OrderDTO> orders = orderClient.getAllOrders().getData();

        // Total revenue excluding cancelled and failed orders.
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED
                        && o.getOrderStatus() != OrderStatus.FAILED)
                .map(OrderDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total valid orders count.
        long totalOrders = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED
                        && o.getOrderStatus() != OrderStatus.FAILED)
                .count();

        // Average order value.
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Revenue grouped by status.
        Map<String, BigDecimal> revenueByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderStatus().name(),
                        Collectors.reducing(BigDecimal.ZERO, OrderDTO::getTotalAmount, BigDecimal::add)
                ));

        // Order count grouped by status.
        Map<String, Long> countByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderStatus().name(),
                        Collectors.counting()
                ));

        return RevenueReportDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .revenueByStatus(revenueByStatus)
                .orderCountByStatus(countByStatus)
                .build();
    }

    private RevenueReportDTO getRevenueReportFallback(Throwable throwable) {
        RevenueReportDTO report = new RevenueReportDTO();
        report.setTotalRevenue(BigDecimal.ZERO);
        report.setTotalOrders(0L);
        report.setAverageOrderValue(BigDecimal.ZERO);
        report.setRevenueByStatus(Map.of());
        report.setOrderCountByStatus(Map.of());
        return report;
    }
}

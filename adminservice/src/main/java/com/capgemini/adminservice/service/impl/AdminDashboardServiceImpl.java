package com.capgemini.adminservice.service.impl;

import com.capgemini.adminservice.client.CatlogClient;
import com.capgemini.adminservice.client.OrderClient;
import com.capgemini.adminservice.dto.DashboardDTO;
import com.capgemini.adminservice.dto.OrderDTO;
import com.capgemini.adminservice.enums.OrderStatus;
import com.capgemini.adminservice.service.AdminDashboardService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderClient orderClient;
    private final CatlogClient catalogClient;

    // Opens the circuit if dashboard calls keep failing.
    @CircuitBreaker(name = "adminDashboard", fallbackMethod = "getDashboardFallback")
    // Retries brief dashboard call issues before failing the request.
    @Retry(name = "adminDashboard")
    @Override
    public DashboardDTO getDashboard() {

        // Get all orders from Order Service via Feign
        List<OrderDTO> orders = orderClient.getAllOrders().getData();

        // Calculate order metrics using enum
        long total      = orders.size();
        long pending    = countByStatus(orders, OrderStatus.PENDING);
        long confirmed  = countByStatus(orders, OrderStatus.CONFIRMED);
        long packed     = countByStatus(orders, OrderStatus.PACKED);
        long shipped    = countByStatus(orders, OrderStatus.SHIPPED);
        long delivered  = countByStatus(orders, OrderStatus.DELIVERED);
        long cancelled  = countByStatus(orders, OrderStatus.CANCELLED);
        long failed     = countByStatus(orders, OrderStatus.FAILED);

        // Calculate total revenue from CONFIRMED, DELIVERED, PACKED, SHIPPED orders
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.CONFIRMED
                        || o.getOrderStatus() == OrderStatus.DELIVERED
                        || o.getOrderStatus() == OrderStatus.PACKED
                        || o.getOrderStatus() == OrderStatus.SHIPPED)
                .map(OrderDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get catalog metrics from Catalog Service via Feign
        Long totalProducts   = catalogClient.getProductCount();
        Long totalCategories = catalogClient.getCategoryCount();

        // Low stock count
        Long lowStock = catalogClient.getAllProducts().stream()
                .filter(p -> p.getProductStock() < 5)
                .count();

        // Build DashboardDTO
        return DashboardDTO.builder()
                .totalOrders(total)
                .pendingOrders(pending)
                .confirmedOrders(confirmed)
                .packedOrders(packed)
                .shippedOrders(shipped)
                .deliveredOrders(delivered)
                .cancelledOrders(cancelled)
                .failedOrders(failed)
                .totalRevenue(totalRevenue)
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .lowStockProducts(lowStock)
                .build();
    }

    private DashboardDTO getDashboardFallback(Throwable throwable) {
        log.error("Dashboard fallback triggered: {}", throwable.getMessage());
        return DashboardDTO.builder()
                .totalOrders(0L)
                .pendingOrders(0L)
                .confirmedOrders(0L)
                .packedOrders(0L)
                .shippedOrders(0L)
                .deliveredOrders(0L)
                .cancelledOrders(0L)
                .failedOrders(0L)
                .totalRevenue(BigDecimal.ZERO)
                .totalProducts(0L)
                .totalCategories(0L)
                .lowStockProducts(0L)
                .build();
    }

    // Use enum instead of string
    private long countByStatus(List<OrderDTO> orders, OrderStatus status) {
        return orders.stream()
                .filter(o -> o.getOrderStatus() == status)
                .count();
    }
}

package com.capgemini.adminservice.service.impl;

import com.capgemini.adminservice.client.CatlogClient;
import com.capgemini.adminservice.client.OrderClient;
import com.capgemini.adminservice.dto.ApiResponse;
import com.capgemini.adminservice.dto.DashboardDTO;
import com.capgemini.adminservice.dto.OrderDTO;
import com.capgemini.adminservice.dto.ProductDTO;
import com.capgemini.adminservice.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private OrderClient orderClient;

    @Mock
    private CatlogClient catalogClient;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Test
    void getDashboardAggregatesOrderAndCatalogMetrics() {
        ApiResponse<List<OrderDTO>> orderResponse = new ApiResponse<>();
        orderResponse.setData(List.of(
                OrderDTO.builder()
                        .orderId(1)
                        .orderStatus(OrderStatus.CONFIRMED)
                        .totalAmount(new BigDecimal("1200.00"))
                        .build(),
                OrderDTO.builder()
                        .orderId(2)
                        .orderStatus(OrderStatus.DELIVERED)
                        .totalAmount(new BigDecimal("800.00"))
                        .build(),
                OrderDTO.builder()
                        .orderId(3)
                        .orderStatus(OrderStatus.CANCELLED)
                        .totalAmount(new BigDecimal("300.00"))
                        .build(),
                OrderDTO.builder()
                        .orderId(4)
                        .orderStatus(OrderStatus.PENDING)
                        .totalAmount(new BigDecimal("100.00"))
                        .build()
        ));

        when(orderClient.getAllOrders()).thenReturn(orderResponse);
        when(catalogClient.getProductCount()).thenReturn(12L);
        when(catalogClient.getCategoryCount()).thenReturn(4L);
        when(catalogClient.getAllProducts()).thenReturn(List.of(
                ProductDTO.builder().productId(1L).productName("Phone").productStock(3).build(),
                ProductDTO.builder().productId(2L).productName("Laptop").productStock(8).build(),
                ProductDTO.builder().productId(3L).productName("Mouse").productStock(1).build()
        ));

        DashboardDTO dashboard = adminDashboardService.getDashboard();

        assertEquals(4L, dashboard.getTotalOrders());
        assertEquals(1L, dashboard.getPendingOrders());
        assertEquals(1L, dashboard.getConfirmedOrders());
        assertEquals(1L, dashboard.getDeliveredOrders());
        assertEquals(1L, dashboard.getCancelledOrders());
        assertEquals(new BigDecimal("2000.00"), dashboard.getTotalRevenue());
        assertEquals(12L, dashboard.getTotalProducts());
        assertEquals(4L, dashboard.getTotalCategories());
        assertEquals(2L, dashboard.getLowStockProducts());
    }
}

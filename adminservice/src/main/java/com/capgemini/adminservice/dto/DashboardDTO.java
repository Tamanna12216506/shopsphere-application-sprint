package com.capgemini.adminservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
//    Combines data from Order + Catalog services
// Used to show admin dashboard (revenue, counts, stats)
    // Order metrics
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long packedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Long failedOrders;

    // Revenue
    private BigDecimal totalRevenue;

    // Catalog metrics
    private Long totalProducts;
    private Long totalCategories;
    private Long lowStockProducts;
}

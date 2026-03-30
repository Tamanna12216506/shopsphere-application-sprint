package com.capgemini.adminservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDTO {
    // Holds revenue and report-related data
    // Used in admin reports API
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private Map<String, BigDecimal> revenueByStatus;
    private Map<String, Long> orderCountByStatus;
}

package com.capgemini.adminservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockMessage {
    // Message received from Catalog Service via RabbitMQ
// Indicates that a product is low in stock
    private Long productId;
    private String productName;
    private Integer currentStock;
    private LocalDateTime timestamp;
}

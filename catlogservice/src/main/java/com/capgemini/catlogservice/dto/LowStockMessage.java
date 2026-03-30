package com.capgemini.catlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockMessage {

    private Long productId;
    private String productName;
    private Integer currentStock;
    private LocalDateTime timestamp;
}

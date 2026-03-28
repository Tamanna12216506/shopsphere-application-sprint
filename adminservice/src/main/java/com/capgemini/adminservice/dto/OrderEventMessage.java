package com.capgemini.adminservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {

    private Long orderId;
    private String userId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}

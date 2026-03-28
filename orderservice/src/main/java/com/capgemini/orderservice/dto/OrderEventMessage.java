package com.capgemini.orderservice.dto;

import com.capgemini.orderservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {

    private Long orderId;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}
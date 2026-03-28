package com.capgemini.orderservice.dto;

import com.capgemini.orderservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateMessage {

    private Long orderId;
    private OrderStatus status;
    private String updatedBy;   // admin email
    private LocalDateTime timestamp;
}

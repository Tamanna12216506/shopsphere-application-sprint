package com.capgemini.adminservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateMessage {
    // Message sent to RabbitMQ after order status update
// Used to notify other services asynchronously
    private Long orderId;
    private String status;
    private String updatedBy;
    private LocalDateTime timestamp;
}

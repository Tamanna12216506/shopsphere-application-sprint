package com.capgemini.adminservice.messaging.publisher;

import com.capgemini.adminservice.config.RabbitMQConfig;
import com.capgemini.adminservice.dto.OrderStatusUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Admin updates order status
    // Instead of direct Feign call - publish to RabbitMQ
    // Order Service consumer will pick this up and update
    public void publishStatusUpdate(Long orderId, String status, String adminEmail) {
        OrderStatusUpdateMessage message = OrderStatusUpdateMessage.builder()
                        .orderId(orderId)
                        .status(status)
                        .updatedBy(adminEmail)
                        .timestamp(LocalDateTime.now())
                        .build();

        log.info("Publishing status update for orderId: {} → {}", orderId, status);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_STATUS_UPDATE_ROUTING_KEY,
                message
        );
    }
}

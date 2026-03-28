package com.capgemini.orderservice.messaging.publisher;

import com.capgemini.orderservice.client.RabbitMQConfig;
import com.capgemini.orderservice.dto.OrderEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Publish when order is CONFIRMED
    public void publishOrderConfirmed(OrderEventMessage message) {
        log.info("Publishing ORDER_CONFIRMED event for orderId: {}",
                message.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CONFIRMED_ROUTING_KEY,
                message
        );
    }

    // Publish when order is CANCELLED
    public void publishOrderCancelled(OrderEventMessage message) {
        log.info("Publishing ORDER_CANCELLED event for orderId: {}",
                message.getOrderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY,
                message
        );
    }
}

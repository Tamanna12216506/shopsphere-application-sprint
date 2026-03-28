package com.capgemini.orderservice.messaging.consumer;

import com.capgemini.orderservice.client.RabbitMQConfig;
import com.capgemini.orderservice.dto.OrderStatusUpdateMessage;
import com.capgemini.orderservice.entity.Order;
import com.capgemini.orderservice.exception.ResourceNotFoundException;
import com.capgemini.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusConsumer {

    private final OrderRepository orderRepository;

    // Listens to status update messages published by Admin Service
    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_UPDATE_QUEUE)
    public void consumeOrderStatusUpdate(
            OrderStatusUpdateMessage message) {

        log.info("Received status update for orderId: {} → {}",
                message.getOrderId(), message.getStatus());

        Order order = orderRepository
                .findById(message.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + message.getOrderId()));

        order.setOrderStatus(message.getStatus());
        orderRepository.save(order);

        log.info("Order {} status updated to {}",
                message.getOrderId(), message.getStatus());
    }
}

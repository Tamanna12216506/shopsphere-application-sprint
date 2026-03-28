package com.capgemini.orderservice.messaging.consumer;

import com.capgemini.orderservice.config.RabbitMQConfig;
import com.capgemini.orderservice.dto.OrderStatusUpdateMessage;
import com.capgemini.orderservice.dto.UpdateOrderStatusRequest;
import com.capgemini.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusConsumer {

    private final OrderService orderService;

    // Listens to status update messages published by Admin Service
    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_UPDATE_QUEUE)
    public void consumeOrderStatusUpdate(
            OrderStatusUpdateMessage message) {

        log.info("Received status update for orderId: {} → {}",
                message.getOrderId(), message.getStatus());

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(message.getStatus());
        orderService.updateOrderStatus(message.getOrderId(), request);

        log.info("Order {} status updated to {}",
                message.getOrderId(), message.getStatus());
    }
}

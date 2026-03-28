package com.capgemini.adminservice.messaging.consumer;

import com.capgemini.adminservice.config.RabbitMQConfig;
import com.capgemini.adminservice.dto.LowStockMessage;
import com.capgemini.adminservice.dto.OrderEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminMessageConsumer {

    // Listen for order confirmed events from Order Service
    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRMED_QUEUE)
    public void consumeOrderConfirmed(OrderEventMessage message) {
        log.info("ORDER CONFIRMED received → orderId: {} | amount: {}",
                message.getOrderId(), message.getTotalAmount());

        // Here you can:
        // 1. Update dashboard metrics counter in cache
        // 2. Trigger email notification
        // 3. Log for reporting
    }

    // Listen for order cancelled events from Order Service
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void consumeOrderCancelled(OrderEventMessage message) {
        log.info("ORDER CANCELLED received → orderId: {}",
                message.getOrderId());

        // Here you can:
        // 1. Update dashboard metrics
        // 2. Trigger refund notification
    }

    // Listen for low stock alerts from Catalog Service
    @RabbitListener(queues = RabbitMQConfig.LOW_STOCK_QUEUE)
    public void consumeLowStockAlert(LowStockMessage message) {
        log.warn("LOW STOCK ALERT → Product: {} | Stock: {}",
                message.getProductName(), message.getCurrentStock());

        // Here you can:
        // 1. Flag product on admin dashboard
        // 2. Send alert to admin email
        // 3. Store in a low-stock list for dashboard widget
    }
}

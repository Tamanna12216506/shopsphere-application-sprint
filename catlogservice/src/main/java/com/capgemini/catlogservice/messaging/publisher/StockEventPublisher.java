package com.capgemini.catlogservice.messaging.publisher;


import com.capgemini.catlogservice.config.RabbitMQConfig;
import com.capgemini.catlogservice.dto.LowStockMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLowStockAlert(Long productId, String productName, Integer currentStock) {
        log.warn("LOW STOCK ALERT - Product: {} | Stock: {}", productName, currentStock);

        LowStockMessage message = LowStockMessage.builder()
                .productId(productId)
                .productName(productName)
                .currentStock(currentStock)
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CATALOG_EXCHANGE,
                RabbitMQConfig.LOW_STOCK_ROUTING_KEY,
                message
        );
    }
}

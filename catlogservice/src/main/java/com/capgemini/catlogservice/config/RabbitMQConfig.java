package com.capgemini.catlogservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOW_STOCK_QUEUE = "low-stock-alert-queue";
    public static final String CATALOG_EXCHANGE = "catalog.exchange";
    public static final String LOW_STOCK_ROUTING_KEY = "catalog.stock.low";

    // Stock threshold - publish alert if stock drops below this
    public static final int LOW_STOCK_THRESHOLD = 5;

    // Exchange that receives all catalog-domain events.
    @Bean
    public TopicExchange catalogExchange() {
        return new TopicExchange(CATALOG_EXCHANGE);
    }

    // Durable queue so low-stock alerts survive broker restarts.
    @Bean
    public Queue lowStockQueue() {
        return new Queue(LOW_STOCK_QUEUE, true);
    }

    // Route low-stock events from the exchange into the alert queue.
    @Bean
    public Binding lowStockBinding() {
        return BindingBuilder
                .bind(lowStockQueue())
                .to(catalogExchange())
                .with(LOW_STOCK_ROUTING_KEY);
    }

    // JSON converter keeps producer/consumer payloads language-agnostic.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate publishes catalog messages using JSON serialization.
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

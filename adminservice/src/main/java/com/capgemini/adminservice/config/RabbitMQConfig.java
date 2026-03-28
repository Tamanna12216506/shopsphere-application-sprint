package com.capgemini.adminservice.config;

import org.springframework.amqp.core.*; // Core RabbitMQ classes (Queue, Exchange, Binding)
import org.springframework.amqp.rabbit.connection.ConnectionFactory; // Connection to RabbitMQ
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Used to send messages
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter; // Convert object ↔ JSON
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Marks this as a config class
public class RabbitMQConfig {

    // ── Queue Names (must match other services like Order Service) ─────────────
    public static final String ORDER_STATUS_UPDATE_QUEUE =
            "order-status-update-queue"; // queue for order status updates

    public static final String ORDER_CONFIRMED_QUEUE =
            "order-confirmed-queue"; // queue when order is confirmed

    public static final String ORDER_CANCELLED_QUEUE =
            "order-cancelled-queue"; // queue when order is cancelled

    public static final String LOW_STOCK_QUEUE =
            "low-stock-alert-queue"; // queue for low stock alerts

    // ── Exchanges (entry point where messages are sent) ────────────────────────
    public static final String ORDER_EXCHANGE   = "order.exchange"; // for order-related events
    public static final String CATALOG_EXCHANGE = "catalog.exchange"; // for catalog events

    // ── Routing Keys (decide which queue gets the message) ─────────────────────
    public static final String ORDER_STATUS_UPDATE_ROUTING_KEY =
            "order.status.update";

    public static final String ORDER_CONFIRMED_ROUTING_KEY =
            "order.confirmed";

    public static final String ORDER_CANCELLED_ROUTING_KEY =
            "order.cancelled";

    public static final String LOW_STOCK_ROUTING_KEY =
            "catalog.stock.low";

    // ── Create Exchanges ──────────────────────────────────────────────────────
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
        // TopicExchange = routes messages based on routing key pattern
    }

    @Bean
    public TopicExchange catalogExchange() {
        return new TopicExchange(CATALOG_EXCHANGE);
    }

    // ── Create Queues (where messages are stored temporarily) ─────────────────
    @Bean
    public Queue orderStatusUpdateQueue() {
        return new Queue(ORDER_STATUS_UPDATE_QUEUE, true);
        // true = durable (survives server restart)
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return new Queue(ORDER_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE, true);
    }

    @Bean
    public Queue lowStockQueue() {
        return new Queue(LOW_STOCK_QUEUE, true);
    }

    // ── Bindings (connect exchange → queue using routing key) ─────────────────
    @Bean
    public Binding orderStatusUpdateBinding() {
        return BindingBuilder
                .bind(orderStatusUpdateQueue()) // bind this queue
                .to(orderExchange()) // to this exchange
                .with(ORDER_STATUS_UPDATE_ROUTING_KEY); // using this key
    }

    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder
                .bind(orderConfirmedQueue())
                .to(orderExchange())
                .with(ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder
                .bind(orderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public Binding lowStockBinding() {
        return BindingBuilder
                .bind(lowStockQueue())
                .to(catalogExchange())
                .with(LOW_STOCK_ROUTING_KEY);
    }

    // ── Message Converter (Java object ↔ JSON) ────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
        // So you can send DTO directly instead of manual JSON conversion
    }

    // ── RabbitTemplate (used to publish messages) ─────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // ensures messages are sent as JSON

        return rabbitTemplate;
    }
}
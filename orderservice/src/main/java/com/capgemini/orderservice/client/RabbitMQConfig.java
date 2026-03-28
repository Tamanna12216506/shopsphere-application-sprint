package com.capgemini.orderservice.client;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Queue Names ───────────────────────────────────────────────────────────
    public static final String ORDER_STATUS_UPDATE_QUEUE =
            "order-status-update-queue";
    public static final String ORDER_CONFIRMED_QUEUE =
            "order-confirmed-queue";
    public static final String ORDER_CANCELLED_QUEUE =
            "order-cancelled-queue";

    // ── Exchange Names ────────────────────────────────────────────────────────
    public static final String ORDER_EXCHANGE = "order.exchange";

    // ── Routing Keys ──────────────────────────────────────────────────────────
    public static final String ORDER_STATUS_UPDATE_ROUTING_KEY =
            "order.status.update";
    public static final String ORDER_CONFIRMED_ROUTING_KEY =
            "order.confirmed";
    public static final String ORDER_CANCELLED_ROUTING_KEY =
            "order.cancelled";

    // ── Exchange ──────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // ── Queues ────────────────────────────────────────────────────────────────
    @Bean
    public Queue orderStatusUpdateQueue() {
        return new Queue(ORDER_STATUS_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return new Queue(ORDER_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE, true);
    }

    // ── Bindings ──────────────────────────────────────────────────────────────
    @Bean
    public Binding orderStatusUpdateBinding() {
        return BindingBuilder
                .bind(orderStatusUpdateQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_UPDATE_ROUTING_KEY);
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

    // ── JSON Message Converter ────────────────────────────────────────────────
    // Serialize messages as JSON instead of Java bytes
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

package com.capgemini.orderservice.config;

import com.capgemini.orderservice.dto.OrderStatusUpdateMessage;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(
                "com.capgemini.orderservice.dto",
                "com.capgemini.adminservice.dto"
        );

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(
                "com.capgemini.adminservice.dto.OrderStatusUpdateMessage",
                OrderStatusUpdateMessage.class
        );
        typeMapper.setIdClassMapping(idClassMapping);

        converter.setJavaTypeMapper(typeMapper);
        return converter;
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

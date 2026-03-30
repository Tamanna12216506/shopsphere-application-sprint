package com.capgemini.adminservice.service.impl;


import com.capgemini.adminservice.client.OrderClient;
import com.capgemini.adminservice.dto.*;
import com.capgemini.adminservice.messaging.publisher.OrderStatusPublisher;
import com.capgemini.adminservice.service.AdminOrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderClient orderClient;
    private final OrderStatusPublisher orderStatusPublisher;

    // Opens the circuit if admin order list calls keep failing.
    @CircuitBreaker(name = "orderService", fallbackMethod = "getAllOrdersFallback")
    // Retries brief order list issues before failing the request.
    @Retry(name = "orderService")
    @Override
    public List<OrderDTO> getAllOrders() {
        return orderClient.getAllOrders().getData();
    }

    // Opens the circuit if admin order detail calls keep failing.
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderByIdFallback")
    // Retries brief order detail issues before failing the request.
    @Retry(name = "orderService")
    @Override
    public OrderDTO getOrderById(Long id) {
        return orderClient.getOrderById(id).getData();
    }

    @Override
    public void updateOrderStatus(Long orderId,
                                  String status,
                                  String adminEmail) {
        // Publish to RabbitMQ instead of direct Feign call
        // Order Service consumer will update the status
        orderStatusPublisher.publishStatusUpdate(
                orderId, status, adminEmail);
    }

    private List<OrderDTO> getAllOrdersFallback(Throwable throwable) {
        log.error("Admin order list fallback triggered: {}", throwable.getMessage());
        return Collections.emptyList();
    }

    private OrderDTO getOrderByIdFallback(Long id, Throwable throwable) {
        log.error("Admin order detail fallback triggered for orderId={}: {}", id, throwable.getMessage());
        throw new RuntimeException("Order service is temporarily unavailable");
    }
}

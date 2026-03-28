package com.capgemini.adminservice.service.impl;


import com.capgemini.adminservice.client.OrderClient;
import com.capgemini.adminservice.dto.*;
import com.capgemini.adminservice.messaging.publisher.OrderStatusPublisher;
import com.capgemini.adminservice.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderClient orderClient;
    private final OrderStatusPublisher orderStatusPublisher;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderClient.getAllOrders().getData();
    }

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
}
package com.capgemini.adminservice.service;

import com.capgemini.adminservice.dto.OrderDTO;

import java.util.List;

public interface AdminOrderService {

    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Long id);

    void updateOrderStatus(Long orderId, String status, String adminEmail);
}

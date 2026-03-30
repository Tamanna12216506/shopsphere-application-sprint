package com.capgemini.orderservice.service;

import com.capgemini.orderservice.dto.*;

import java.util.List;

public interface OrderService {

    // checkout → converts user's cart into order, saves address & payment mode, and creates final order
    OrderDTO checkout(String userId, CheckoutRequest request);

// getOrderById → fetches a specific order of the user (ensures user owns the order)
    OrderDTO getOrderById(String userId, Long orderId);

// getMyOrders → returns list of all orders placed by the user (order history)
    List<OrderDTO> getMyOrders(String userId);

// cancelOrder → allows user to cancel their order (only if allowed by current status)
    OrderDTO cancelOrder(String userId, Long orderId);

// getAllOrders → admin API to fetch all orders in system
    List<OrderDTO> getAllOrders();

// getOrderByIdForAdmin → admin API to fetch one order by id
    OrderDTO getOrderByIdForAdmin(Long orderId);

// updateOrderStatus → admin updates order status (e.g., CONFIRMED → SHIPPED → DELIVERED)
    OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}

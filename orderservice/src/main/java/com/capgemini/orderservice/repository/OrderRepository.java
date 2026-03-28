package com.capgemini.orderservice.repository;

import com.capgemini.orderservice.entity.Order;
import com.capgemini.orderservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    //Customer order history - sorted newest first
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    //Admin - filter by orderStatus
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    //Admin - all orders newest first
    List<Order> findAllByOrderByCreatedAtDesc();
}

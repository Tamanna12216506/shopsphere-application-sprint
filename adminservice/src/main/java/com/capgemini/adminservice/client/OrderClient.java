package com.capgemini.adminservice.client;

import com.capgemini.adminservice.dto.ApiResponse;
import com.capgemini.adminservice.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="ORDERSERVICE")
public interface OrderClient {
    // Get all orders for admin view
    @GetMapping("/api/orders/admin/all")
    ApiResponse<List<OrderDTO>> getAllOrders();


    // Get single order detail
    @GetMapping("/api/orders/admin/{id}")
    ApiResponse<OrderDTO> getOrderById(@PathVariable Long id);
}

package com.capgemini.adminservice.controller;


import com.capgemini.adminservice.dto.OrderDTO;
import com.capgemini.adminservice.dto.UpdateOrderStatusRequest;
import com.capgemini.adminservice.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService orderService;

    // GET /api/admin/orders
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // GET /api/admin/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // PUT /api/admin/orders/{id}/status
    // Admin email from Gateway header - knows who made the change
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request, @RequestHeader("X-Authenticated-User") String adminEmail) {

        orderService.updateOrderStatus(id, request.getStatus(), adminEmail);

        return ResponseEntity.ok(
                "Order status update queued successfully");
    }
}

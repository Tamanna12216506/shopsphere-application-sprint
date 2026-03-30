package com.capgemini.orderservice.controller;


import com.capgemini.orderservice.dto.*;
import com.capgemini.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Creates order from cart, processes payment, and returns final order details
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(@RequestHeader("X-Authenticated-User") String userId,
                                                          @Valid @RequestBody CheckoutRequest request) {

        OrderDTO orderDTO = orderService.checkout(userId, request);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                        .status(200)
                        .message("Order placed successfully")
                        .data(orderDTO).build()
        );
    }

    // Fetches all orders placed by the logged-in user
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(@RequestHeader("X-Authenticated-User") String userId) {

        List<OrderDTO> orders = orderService.getMyOrders(userId);
        return ResponseEntity.ok(ApiResponse.<List<OrderDTO>>builder()
                        .status(200)
                        .message("Orders fetched successfully")
                        .data(orders).build()
        );
    }

    // Fetches a specific order of the logged-in user
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@RequestHeader("X-Authenticated-User") String userId,
                                                              @PathVariable Long id) {

        OrderDTO orderDTO = orderService.getOrderById(userId, id);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                        .status(200)
                        .message("Order fetched successfully")
                        .data(orderDTO).build()
        );
    }

    // Cancels a user order if cancellation is allowed for current order status
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@RequestHeader("X-Authenticated-User") String userId,
                                                             @PathVariable Long id) {

        OrderDTO orderDTO = orderService.cancelOrder(userId, id);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                        .status(200)
                        .message("Order cancelled successfully")
                        .data(orderDTO).build()
        );
    }

    // Admin API to fetch all orders in the system
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {

        List<OrderDTO> orders = orderService.getAllOrders();

        return ResponseEntity.ok(ApiResponse.<List<OrderDTO>>builder()
                        .status(200)
                        .message("All orders fetched successfully")
                        .data(orders).build()
        );
    }

    // Admin API to fetch one order by id
    @GetMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByIdForAdmin(@PathVariable Long id) {

        OrderDTO orderDTO = orderService.getOrderByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                        .status(200)
                        .message("Order fetched successfully")
                        .data(orderDTO).build()
        );
    }

    // Admin API to update status of a specific order
    @PutMapping("/admin/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderDTO orderDTO = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                        .status(200)
                        .message("Order status updated successfully")
                        .data(orderDTO).build()
        );
    }
}

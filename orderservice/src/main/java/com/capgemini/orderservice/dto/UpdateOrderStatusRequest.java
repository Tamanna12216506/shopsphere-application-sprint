package com.capgemini.orderservice.dto;

import com.capgemini.orderservice.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    // UpdateOrderStatusRequest → used by admin to update only order status

    @NotNull(message = "Status is required")
    private OrderStatus status;
}

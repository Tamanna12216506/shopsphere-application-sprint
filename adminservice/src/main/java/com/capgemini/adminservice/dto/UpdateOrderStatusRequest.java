package com.capgemini.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    // Request payload for updating order status
// Contains new status sent by admin (e.g., SHIPPED, DELIVERED)
    @NotBlank(message = "Status is required")
    private String status;
}

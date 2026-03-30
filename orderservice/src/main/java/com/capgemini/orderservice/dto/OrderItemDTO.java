package com.capgemini.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // OrderItemDTO → response DTO representing each item in an order

    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price; // price per unit
    private BigDecimal subtotal; // price * quantity
}

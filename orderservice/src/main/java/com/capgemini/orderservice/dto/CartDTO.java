package com.capgemini.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDTO {

    // CartDTO → response DTO representing full cart with totalAmount and totalItems

    private Long cartId;
    private String userId;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice; // sum of all items subtotals
    private Integer totalItems; // sum of all item quantities

}

package com.capgemini.orderservice.dto;

import com.capgemini.orderservice.entity.DeliveryAddress;
import com.capgemini.orderservice.enums.OrderStatus;
import com.capgemini.orderservice.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    // OrderDTO → full order response including items, payment, address, and status

    private Integer orderId;
    private String userId;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private PaymentMode paymentMode;
    private String paymentId;
    private DeliveryAddressDTO deliveryAddress;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime createdAt;

}

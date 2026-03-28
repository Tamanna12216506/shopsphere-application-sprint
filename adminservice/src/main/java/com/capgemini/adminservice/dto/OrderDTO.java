package com.capgemini.adminservice.dto;


import com.capgemini.adminservice.enums.OrderStatus;
import com.capgemini.adminservice.enums.PaymentMode;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
// Represents order data received from Order Service
// Used to display and manage orders in admin panel
    ///  it will mirror order service response
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

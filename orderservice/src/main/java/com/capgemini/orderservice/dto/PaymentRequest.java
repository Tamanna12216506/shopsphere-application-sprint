package com.capgemini.orderservice.dto;

import com.capgemini.orderservice.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    // PaymentRequest → internal DTO sent from Order Service to Payment Service

    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private PaymentMode paymentMode;
}

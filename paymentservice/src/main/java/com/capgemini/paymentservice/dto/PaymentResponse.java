package com.capgemini.paymentservice.dto;

import com.capgemini.paymentservice.enums.PaymentMode;
import com.capgemini.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    // Whether payment was successful - used by Order Service
    private boolean success;

    // paymentId from Payment entity
    private Long paymentId;

    // Transaction ID returned to Order Service
    // Order Service stores this in its order record
    private String transactionId;

    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private PaymentStatus paymentStatus;
    private String message;
    private String failureReason;
    private LocalDateTime createdAt;
}
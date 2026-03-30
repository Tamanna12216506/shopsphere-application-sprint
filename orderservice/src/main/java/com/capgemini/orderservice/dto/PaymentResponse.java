package com.capgemini.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

    // PaymentResponse → internal DTO received from Payment Service after processing

    private boolean success;
    private String message;
    private String paymentId;
    private String transactionId;
}

package com.capgemini.paymentservice.service;


import com.capgemini.paymentservice.dto.PaymentRequest;
import com.capgemini.paymentservice.dto.PaymentResponse;
import com.capgemini.paymentservice.dto.RefundRequest;

import java.util.List;

public interface PaymentService {

    // Process payment - called by Order Service via Feign
    PaymentResponse processPayment(PaymentRequest request);

    // Get payment by orderId - Order Service or Admin checks status
    PaymentResponse getPaymentByOrderId(Long orderId);

    // Get payment history for a user
    List<PaymentResponse> getPaymentHistory(String userId);

    // Process refund when order is cancelled
    PaymentResponse processRefund(RefundRequest request);
}
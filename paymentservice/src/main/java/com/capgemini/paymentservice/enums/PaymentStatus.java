package com.capgemini.paymentservice.enums;

public enum PaymentStatus {
    PENDING,    // payment initiated but not processed
    SUCCESS,    // payment successful
    FAILED,     // payment failed
    REFUNDED    // payment refunded (on order cancellation)
}

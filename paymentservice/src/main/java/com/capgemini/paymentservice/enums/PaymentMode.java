package com.capgemini.paymentservice.enums;

public enum PaymentMode {
    CARD,   // Credit / Debit Card - simulated with 10% failure rate
    UPI,    // UPI Payment - simulated with 10% failure rate
    COD     // Cash on Delivery - always SUCCESS, no upfront payment
}
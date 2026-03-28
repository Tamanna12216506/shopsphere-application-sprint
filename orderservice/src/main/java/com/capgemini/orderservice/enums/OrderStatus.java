package com.capgemini.orderservice.enums;

public enum OrderStatus {
    PENDING,  /// order created, waiting for payment method
    CONFIRMED, /// payment successful
    PACKED,    ///  order packed and ready for shipment
    SHIPPED,   /// order shipped to customer
    DELIVERED, /// order delivered to customer and admin marked delivered
    CANCELLED,  /// Customer cancelled the order before shipment or admin cancelled
    FAILED      ///payment failed
}

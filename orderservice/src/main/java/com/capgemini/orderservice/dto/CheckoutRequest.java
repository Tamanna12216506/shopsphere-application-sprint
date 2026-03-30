package com.capgemini.orderservice.dto;

import com.capgemini.orderservice.enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    // CheckoutRequest → wraps delivery address and payment mode for placing order

    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDTO deliveryAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMode  paymentMode;
}

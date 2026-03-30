package com.capgemini.orderservice.client;


import com.capgemini.orderservice.dto.PaymentRequest;
import com.capgemini.orderservice.dto.PaymentResponse;
import com.capgemini.orderservice.dto.RefundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name = service name registered in Eureka
// Feign + Eureka auto resolves to correct host:port
@FeignClient(name = "PAYMENTSERVICE")
public interface PaymentClient {

    @PostMapping("/api/payment/process")
    PaymentResponse processPayment(
            @RequestBody PaymentRequest request);

    @PostMapping("/api/payment/refund")
    PaymentResponse processRefund(
            @RequestBody RefundRequest request);
}

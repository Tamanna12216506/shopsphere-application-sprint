package com.capgemini.paymentservice.controller;

import com.capgemini.paymentservice.dto.PaymentRequest;
import com.capgemini.paymentservice.dto.PaymentResponse;
import com.capgemini.paymentservice.dto.RefundRequest;
import com.capgemini.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(
            @RequestHeader("X-Authenticated-User") String userId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userId));
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> processRefund(
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.processRefund(request));
    }
}

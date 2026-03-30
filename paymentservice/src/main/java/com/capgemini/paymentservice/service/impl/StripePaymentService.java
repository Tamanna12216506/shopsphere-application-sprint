package com.capgemini.paymentservice.service.impl;

import com.capgemini.paymentservice.dto.PaymentRequest;
import com.capgemini.paymentservice.dto.PaymentResponse;
import com.capgemini.paymentservice.enums.PaymentMode;
import com.capgemini.paymentservice.enums.PaymentStatus;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    public PaymentResponse processPayment(PaymentRequest request) {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe payment started for orderId={} userId={} amount={} mode={}",
                request.getOrderId(), request.getUserId(), request.getAmount(), request.getPaymentMode());

        try {
            // Stripe expects amount in smallest currency unit
            long amountInPaise = request.getAmount().multiply(new BigDecimal(100)).longValue();

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amountInPaise);
            params.put("currency", "inr");
            params.put("payment_method", "pm_card_visa");  // Stripe test card
            params.put("confirmation_method", "automatic");
            params.put("confirm", true);
            params.put("metadata", Map.of(
                    "order_id", request.getOrderId().toString(),
                    "user_id", request.getUserId()
            ));

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Determine success based on Stripe status
            boolean success = "succeeded".equalsIgnoreCase(paymentIntent.getStatus());

            if (success) {
                log.info("Stripe payment succeeded for orderId={} transactionId={}",
                        request.getOrderId(), paymentIntent.getId());
            } else {
                log.warn("Stripe payment did not succeed for orderId={} status={} transactionId={}",
                        request.getOrderId(), paymentIntent.getStatus(), paymentIntent.getId());
            }

            return PaymentResponse.builder()
                    .success(success)
                    .transactionId(paymentIntent.getId())
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .paymentMode(request.getPaymentMode())
                    .paymentStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                    .message(success ? "Payment successful" : "Payment failed")
                    .failureReason(success ? null : "Stripe payment status: " + paymentIntent.getStatus())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe exception for orderId={}: {}", request.getOrderId(), e.getMessage());
            return PaymentResponse.builder()
                    .success(false)
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .paymentMode(request.getPaymentMode())
                    .paymentStatus(PaymentStatus.FAILED)
                    .message("Payment failed due to Stripe exception")
                    .failureReason(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }
}

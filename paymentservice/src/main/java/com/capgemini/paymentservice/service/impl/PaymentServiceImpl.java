package com.capgemini.paymentservice.service.impl;

import com.capgemini.paymentservice.dto.PaymentRequest;
import com.capgemini.paymentservice.dto.PaymentResponse;
import com.capgemini.paymentservice.dto.RefundRequest;
import com.capgemini.paymentservice.entity.Payment;
import com.capgemini.paymentservice.enums.PaymentMode;
import com.capgemini.paymentservice.enums.PaymentStatus;
import com.capgemini.paymentservice.exception.BadRequestException;
import com.capgemini.paymentservice.exception.ResourceNotFoundException;
import com.capgemini.paymentservice.repository.PaymentRepository;
import com.capgemini.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripePaymentService stripePaymentService;

    @Value("${payment.simulation.failure-rate}")
    private double failureRate;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for orderId: {} | mode: {} | amount: {}",
                request.getOrderId(), request.getPaymentMode(), request.getAmount());

        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BadRequestException("Payment already processed for orderId: " + request.getOrderId());
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (request.getPaymentMode() == PaymentMode.CARD) {
            PaymentResponse stripeResponse = stripePaymentService.processPayment(request);
            savedPayment.setPaymentStatus(stripeResponse.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
            savedPayment.setTransactionId(stripeResponse.getTransactionId());
            savedPayment.setFailureReason(stripeResponse.getFailureReason());
        } else {
            boolean isSuccess = simulatePayment(request.getPaymentMode());

            if (isSuccess) {
                String transactionId = generateTransactionId(request.getPaymentMode());
                savedPayment.setPaymentStatus(PaymentStatus.SUCCESS);
                savedPayment.setTransactionId(transactionId);
                log.info("Payment SUCCESS for orderId: {} | txnId: {}", request.getOrderId(), transactionId);
            } else {
                String failureReason = getFailureReason(request.getPaymentMode());
                savedPayment.setPaymentStatus(PaymentStatus.FAILED);
                savedPayment.setFailureReason(failureReason);
                log.warn("Payment FAILED for orderId: {} | reason: {}", request.getOrderId(), failureReason);
            }
        }

        Payment finalPayment = paymentRepository.save(savedPayment);
        return toPaymentResponse(finalPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for orderId: " + orderId));
        return toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse processRefund(RefundRequest request) {
        log.info("Processing refund for orderId: {}", request.getOrderId());

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for orderId: " + request.getOrderId()));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Cannot refund payment with status: "
                    + payment.getPaymentStatus() + ". Only SUCCESS payments can be refunded.");
        }

        if (payment.getPaymentMode() == PaymentMode.COD) {
            throw new BadRequestException("COD orders do not require payment refund");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setFailureReason("Refunded: "
                + (request.getReason() != null ? request.getReason() : "Order cancelled"));

        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Refund SUCCESS for orderId: {}", request.getOrderId());
        return toPaymentResponse(refundedPayment);
    }

    private boolean simulatePayment(PaymentMode mode) {
        switch (mode) {
            case COD:
                return true;
            case CARD:
            case UPI:
                int randomNumber = new Random().nextInt(100);
                boolean success = randomNumber >= (failureRate * 100);
                log.debug("Payment simulation - mode: {} | random: {} | failureRate: {} | success: {}",
                        mode, randomNumber, failureRate, success);
                return success;
            default:
                return false;
        }
    }

    private String generateTransactionId(PaymentMode mode) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String prefix = switch (mode) {
            case CARD -> "CARD";
            case UPI -> "UPI";
            case COD -> "COD";
        };

        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + timestamp + "-" + uniquePart;
    }

    private String getFailureReason(PaymentMode mode) {
        String[] cardFailures = {
                "Insufficient balance",
                "Card declined by bank",
                "Invalid card details",
                "Transaction limit exceeded"
        };

        String[] upiFailures = {
                "UPI PIN incorrect",
                "UPI ID not found",
                "Bank server timeout",
                "Daily limit exceeded"
        };

        Random random = new Random();

        return switch (mode) {
            case CARD -> cardFailures[random.nextInt(cardFailures.length)];
            case UPI -> upiFailures[random.nextInt(upiFailures.length)];
            default -> "Payment failed";
        };
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .success(payment.getPaymentStatus() == PaymentStatus.SUCCESS)
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .paymentStatus(payment.getPaymentStatus())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .message(payment.getPaymentStatus() == PaymentStatus.SUCCESS
                        ? "Payment processed successfully"
                        : payment.getPaymentStatus() == PaymentStatus.REFUNDED
                        ? "Payment refunded successfully"
                        : "Payment failed: " + payment.getFailureReason())
                .build();
    }
}

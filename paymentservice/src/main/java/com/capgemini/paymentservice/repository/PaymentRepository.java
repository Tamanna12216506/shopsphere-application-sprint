package com.capgemini.paymentservice.repository;


import com.capgemini.paymentservice.entity.Payment;
import com.capgemini.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by orderId - one order has one payment
    Optional<Payment> findByOrderId(Long orderId);

    // Find all payments for a user - payment history
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find all payments by status
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
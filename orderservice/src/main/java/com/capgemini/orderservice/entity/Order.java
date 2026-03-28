package com.capgemini.orderservice.entity;

import com.capgemini.orderservice.enums.OrderStatus;
import com.capgemini.orderservice.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// Order entity represents final confirmed purchase made by user
// Stores user info, payment details, delivery address, and ordered items

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    // Primary key for order
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // User email (taken from gateway header) who placed the order
    @Column(nullable = false)
    private String userId;

    // Current status of order (PENDING, CONFIRMED, SHIPPED, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    // Total amount calculated from all order items
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Mode of payment selected by user (COD, UPI, CARD)
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    // Payment transaction ID returned from Payment Service
    private String paymentId;

    // Delivery address associated with this order (one-to-one relation)
    // Cascade ensures address is saved/deleted along with order
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_address_id")
    private DeliveryAddress deliveryAddress;

    // List of items in this order
    // Cascade saves/deletes items with order
    // orphanRemoval removes items if detached from order
    // LAZY loading improves performance
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items=new ArrayList<>();

    // Automatically stores order creation timestamp
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Automatically updates timestamp when order is modified
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
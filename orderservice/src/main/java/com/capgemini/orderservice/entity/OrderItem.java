package com.capgemini.orderservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    // Primary key for order item
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    // Many items belong to one order (foreign key: order_id)
    // LAZY loading improves performance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Product reference from Catalog Service
    // Stored as snapshot to avoid dependency on external service
    @Column(nullable = false)
    private Long productId;

    // Product name snapshot at time of order (remains unchanged)
    @Column(nullable = false)
    private String productName;

    // Product image snapshot
    private String productImageUrl;

    // Price snapshot at time of ordering (never updated later)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Quantity of product ordered
    @Column(nullable = false)
    private Integer quantity;

    // Calculated field: price * quantity (stored for faster reads)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}

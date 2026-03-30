package com.capgemini.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long CartItemId;

    // Reference to parent cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id",nullable = false)
    private Cart cart;

    // Use snapshot to keep cart/order data consistent, avoid dependency on Catalog Service, and prevent price/name changes from affecting user’s existing cart or past orders
    // Snapshot from catalog service at time of adding
    // we store these os cart doesn't depend on catalog service
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    private String productImageUrl;

    // Price snapshot - if product price changes, cart price stays same
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

}

package com.capgemini.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Cart entity represents a user's shopping cart (temporary storage before placing order)

@Entity
@Table(name="carts")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    /// User email from X-Authenticated-User header
    /// Stores user email (from gateway header) → ensures one cart per user
    @Column(nullable = false,unique = true)
    private String userId;

    // One cart can have multiple items - cascade ensures items are saved/delted with cart
    // orphanRemoval removes the items if they are removed from the cart's item list
    // Lazy fetching to load items only when needed
    // mappedBy = "cart" refers to the field name in CartItem entity (not table or class name), meaning CartItem owns the relationship via its 'cart' field
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval=true,fetch = FetchType.LAZY)
    @Builder.Default
    // @Builder.Default tells Lombok to keep the initialized value when using @Builder
    // Without this, fields like List<CartItem> items = new ArrayList<>() become null when built
    // It ensures default values are not lost during object creation via builder pattern
    private List<CartItem> items=new ArrayList<>();

    @Column(nullable = false,updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}

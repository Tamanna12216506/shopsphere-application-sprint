package com.capgemini.catlogservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String productDescription;

    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal productPrice;

    @Column(nullable = false)
    private Integer productStock;

    private String brand;

    private String imageUrl;


    // product is available or not
    @Column(nullable = false)
    private Boolean isAvailable=true;

    /// whether product is highlighted or not (trending product)
    @Column(nullable = false)
    private boolean featured = false;

    /// Many Product one category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;






}

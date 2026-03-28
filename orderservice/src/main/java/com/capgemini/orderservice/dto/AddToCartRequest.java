package com.capgemini.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddToCartRequest {

// AddToCartRequest → used to add product to cart with validation (productId + quantity)

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

//    @NotBlank(message = "Product name cannot be blank")
//    private String productName;
//
//    private String productImageUrl;
//
//    @NotNull(message = "Price is required")
//    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
//    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

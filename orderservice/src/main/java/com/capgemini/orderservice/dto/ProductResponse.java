package com.capgemini.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private Integer productStock;
    private String brand;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Boolean available;
}
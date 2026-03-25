package com.capgemini.catlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private  Integer productStock;
    private String brand;
    private String imageUrl;
    private LocalDateTime  createdAt;
    private String categoryName;
    private boolean isAvailable;
}

package com.capgemini.adminservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    // Represents product data from Catalog Service
// Used for product listing and admin operations
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private  Integer productStock;
    private String brand;
    private String imageUrl;
    private LocalDateTime  createdAt;
    private Long categoryId;
    private String categoryName;
    private boolean isAvailable;
}

package com.capgemini.catlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long customerId;
    private String categoryName;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
}

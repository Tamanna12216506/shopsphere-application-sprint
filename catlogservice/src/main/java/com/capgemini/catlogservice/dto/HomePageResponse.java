package com.capgemini.catlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageResponse {

    private List<CategoryResponse> categories;
    private List<FeaturedProductResponse> featuredProducts;
}

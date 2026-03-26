package com.capgemini.catlogservice.service.impl;

import com.capgemini.catlogservice.dto.CategoryResponse;
import com.capgemini.catlogservice.dto.FeaturedProductResponse;
import com.capgemini.catlogservice.dto.HomePageResponse;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.repository.ProductRepository;
import com.capgemini.catlogservice.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public HomePageResponse getHomePageData() {
        /// categories
        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream().map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();

// Inline mapping: ModelMapper for basic fields + manual mapping for nested categoryName
        /// featured Product
        List<FeaturedProductResponse> featuredProducts = productRepository.findByFeaturedTrueAndIsAvailableTrue()
                .stream()
                .map(product -> {FeaturedProductResponse featuredProduct = modelMapper.map(product, FeaturedProductResponse.class);
                 featuredProduct.setCategoryName(product.getCategory().getCategoryName());
                 return featuredProduct;
                }).toList();

        return  HomePageResponse.builder().categories(categories).featuredProducts(featuredProducts).build();
    }
}

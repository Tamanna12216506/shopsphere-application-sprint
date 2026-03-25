package com.capgemini.catlogservice.service;

import com.capgemini.catlogservice.dto.ProductRequest;
import com.capgemini.catlogservice.dto.ProductResponse;
import org.springframework.data.domain.Page;


public interface ProductService {
    Page<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);
}

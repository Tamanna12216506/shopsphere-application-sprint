package com.capgemini.catlogservice.service;

import com.capgemini.catlogservice.dto.ProductRequest;
import com.capgemini.catlogservice.dto.ProductResponse;
import org.springframework.data.domain.Page;


public interface ProductService {
    Page<ProductResponse> getAllProducts(String search,Long categoryId,int page,int size,String sortBy,String sortDir);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);
}

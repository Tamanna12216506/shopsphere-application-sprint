package com.capgemini.catlogservice.service;

import com.capgemini.catlogservice.dto.ProductRequest;
import com.capgemini.catlogservice.dto.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;


public interface ProductService {
    Page<ProductResponse> getAllProducts(String search,Long categoryId,int page,int size,String sortBy,String sortDir);
    List<ProductResponse> getAllAvailableProducts();
    Long getAvailableProductCount();
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);

    ProductResponse markAsFeatured(Long id);

    // Add this to existing ProductService interface
    void reduceStock(Long productId, Integer quantity);

    // Also add stock check
//    ProductResponse getProductWithStockCheck(Long productId, Integer requiredQuantity);
    void increaseStock(Long productId, Integer quantity);
}

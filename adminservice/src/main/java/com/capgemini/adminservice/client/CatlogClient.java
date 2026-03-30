package com.capgemini.adminservice.client;


import com.capgemini.adminservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "CATALOGSERVICE")
public interface CatlogClient {

    // Get all products
    @GetMapping("/api/catalog/products/all")
    List<ProductDTO> getAllProducts();

    // Get total product count
    @GetMapping("/api/catalog/products/count")
    Long getProductCount();

    // Get total category count
    @GetMapping("/api/catalog/categories/count")
    Long getCategoryCount();
}

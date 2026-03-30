package com.capgemini.orderservice.client;

import com.capgemini.orderservice.dto.ApiResponse;
import com.capgemini.orderservice.dto.ProductResponse;
import com.capgemini.orderservice.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CATALOGSERVICE")
public interface CatalogClient {
    @GetMapping("/api/catalog/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable Long id);

    @PutMapping("/api/admin/catalog/products/{id}/stock/reduce")
    void reduceStock(@PathVariable("id") Long id,
                     @RequestBody StockUpdateRequest request);

    @PutMapping("/api/admin/catalog/products/{id}/stock/increase")
    void increaseStock(@PathVariable("id") Long id,
                       @RequestBody StockUpdateRequest request);
}

package com.capgemini.catlogservice.controller;

import com.capgemini.catlogservice.dto.*;
import com.capgemini.catlogservice.service.CategoryService;
import com.capgemini.catlogservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/catalog")
public class AdminProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    /// product end points for create ,update and delete
    /// Gateway server checks admin role before this even runs

    /// add new product
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.createProduct(productRequest);
        return new ResponseEntity<>(new ApiResponse<>(201,"Product created successfully", productResponse), HttpStatus.CREATED);
    }

    /// update
    @PutMapping("/products/id")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        return new ResponseEntity<>(new ApiResponse<>(200,"Product updated successfully", productResponse), HttpStatus.OK);
    }

    /// Delete
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct( @PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(new ApiResponse<>(200,"Product deleted successfully", null), HttpStatus.OK);
    }
    // for featured
    @PatchMapping("/products/{id}/featured")
    public ResponseEntity<ApiResponse<ProductResponse>> markFeatured(@PathVariable Long id) {
        ProductResponse response = productService.markAsFeatured(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Product marked as featured", response));
    }
    /// category endpoints

    /// add new category
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> addCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);
        return new ResponseEntity<>(new ApiResponse<>(201,"Category created successfully", categoryResponse), HttpStatus.CREATED);
    }

    /// update
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, categoryRequest);
        return new ResponseEntity<>(new ApiResponse<>(200,"Category updated successfully", categoryResponse), HttpStatus.OK);
    }

    /// delete
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory( @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(new ApiResponse<>(200,"Category deleted successfully", null), HttpStatus.OK);
    }
}

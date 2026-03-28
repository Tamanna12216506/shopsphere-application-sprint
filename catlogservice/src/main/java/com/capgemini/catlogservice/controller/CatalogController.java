package com.capgemini.catlogservice.controller;

import com.capgemini.catlogservice.dto.ApiResponse;
import com.capgemini.catlogservice.dto.CategoryResponse;
import com.capgemini.catlogservice.dto.HomePageResponse;
import com.capgemini.catlogservice.dto.ProductResponse;
import com.capgemini.catlogservice.service.CategoryService;
import com.capgemini.catlogservice.service.HomeService;
import com.capgemini.catlogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog")
public class CatalogController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final HomeService homeService;

    /// mapping for accessing the home page - return categories and featuredProduct
    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomePageResponse>> getHomePageData() {
        HomePageResponse homePageResponse = homeService.getHomePageData();
        ApiResponse<HomePageResponse> apiResponse = new ApiResponse<>(200,"Home page data successfully", homePageResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /// product listing page
    ///  params: ?serach=laptop&categoryId&page=0&size=10&sortBy=price&sortDir=asc
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<ProductResponse> products = productService.getAllProducts(search, categoryId, page, size, sortBy, sortDir);

        ApiResponse<Page<ProductResponse>> response = new ApiResponse<>(200, "Products fetched successfully", products);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Used by adminservice Feign client to fetch all active products in one call.
    @GetMapping("/products/all")
    public ResponseEntity<List<ProductResponse>> getAllAvailableProducts() {
        List<ProductResponse> products = productService.getAllAvailableProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/products/count")
    public ResponseEntity<Long> getProductCount() {
        Long productCount = productService.getAvailableProductCount();
        return new ResponseEntity<>(productCount, HttpStatus.OK);
    }

    /// product detail page
    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse productResponse = productService.getProductById(id);

        return new ApiResponse<>(200, "Product fetched successfully", productResponse);

    }

    // for getting all categories
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> response = new ApiResponse<>(200, "Categories fetched successfully", categories);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/categories/count")
    public ResponseEntity<Long> getCategoryCount() {
        Long categoryCount = categoryService.getCategoryCount();
        return new ResponseEntity<>(categoryCount, HttpStatus.OK);
    }


    //getting category by id
    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(200, "Category fetched successfully", categoryResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}

package com.capgemini.catlogservice.service;

import com.capgemini.catlogservice.dto.CategoryRequest;
import com.capgemini.catlogservice.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(Long id,CategoryRequest categoryRequest);

    void deleteCategory(Long id);
}

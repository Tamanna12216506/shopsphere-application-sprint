package com.capgemini.catlogservice.service.impl;

import com.capgemini.catlogservice.dto.CategoryRequest;
import com.capgemini.catlogservice.dto.CategoryResponse;
import com.capgemini.catlogservice.entity.Category;
import com.capgemini.catlogservice.exception.ResourceNotFoundException;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CateogoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public Long getCategoryCount() {
        return categoryRepository.count();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("Category not found for id={}", id);
            return new ResourceNotFoundException("Category not found with id: " + id);
        });
        return modelMapper.map(category,CategoryResponse.class);
    }


    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        log.info("Creating category with name={}", categoryRequest.getCategoryName());
        Category category = modelMapper.map(categoryRequest,Category.class);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created with id={}", savedCategory.getCategoryId());
        return modelMapper.map(savedCategory,CategoryResponse.class);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        log.info("Updating category id={}", id);
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("Cannot update. Category not found for id={}", id);
            return new ResourceNotFoundException("Category not found with id: " + id);
        });
        modelMapper.map(categoryRequest,category);
        Category updateCategory=categoryRepository.save(category);
        log.info("Category updated id={}", updateCategory.getCategoryId());
        return modelMapper.map(updateCategory,CategoryResponse.class);
    }


    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category id={}", id);
        if(!categoryRepository.existsById(id)) {
            log.warn("Cannot delete. Category not found for id={}", id);
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Category deleted id={}", id);
    }
}

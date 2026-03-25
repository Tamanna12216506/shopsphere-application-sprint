package com.capgemini.catlogservice.service.impl;

import com.capgemini.catlogservice.dto.CategoryRequest;
import com.capgemini.catlogservice.dto.CategoryResponse;
import com.capgemini.catlogservice.entity.Category;
import com.capgemini.catlogservice.exception.ResourceNotFoundException;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Category not found with id: "+id));
        return modelMapper.map(category,CategoryResponse.class);
    }


    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = modelMapper.map(categoryRequest,Category.class);
        categoryRepository.save(category);
        return modelMapper.map(category,CategoryResponse.class);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Category not found with id: "+id));
        modelMapper.map(categoryRequest,category);
        Category updateCategory=categoryRepository.save(category);
        return modelMapper.map(updateCategory,CategoryResponse.class);
    }


    @Override
    public void deleteCategory(Long id) {
        if(!categoryRepository.existsById(id)){
            throw new ResourceNotFoundException("Category not found with id: "+id);
        }
        categoryRepository.deleteById(id);
    }
}

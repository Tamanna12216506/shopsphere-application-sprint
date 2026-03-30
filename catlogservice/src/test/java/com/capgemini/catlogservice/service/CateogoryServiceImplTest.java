package com.capgemini.catlogservice.service;

import com.capgemini.catlogservice.dto.CategoryRequest;
import com.capgemini.catlogservice.dto.CategoryResponse;
import com.capgemini.catlogservice.entity.Category;
import com.capgemini.catlogservice.exception.ResourceNotFoundException;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.service.impl.CateogoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CateogoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CateogoryServiceImpl categoryService;

    private Category mockCategory;
    private CategoryRequest mockCategoryRequest;
    private CategoryResponse mockCategoryResponse;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setCategoryId(1L);
        mockCategory.setCategoryName("Electronics");
        mockCategory.setDescription("Electronic products");
        mockCategory.setImageUrl("electronics.jpg");

        mockCategoryRequest = new CategoryRequest();
        mockCategoryRequest.setCategoryName("Electronics");
        mockCategoryRequest.setDescription("Electronic products");
        mockCategoryRequest.setImageUrl("electronics.jpg");

        mockCategoryResponse = CategoryResponse.builder()
                .categoryId(1L)
                .categoryName("Electronics")
                .description("Electronic products")
                .imageUrl("electronics.jpg")
                .build();
    }

    @Test
    void getAllCategories_ShouldReturnCategoryList_WhenCategoriesExist() {
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));
        when(modelMapper.map(mockCategory, CategoryResponse.class)).thenReturn(mockCategoryResponse);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCategoryName());

        verify(categoryRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(mockCategory, CategoryResponse.class);
    }

    @Test
    void getCategoryCount_ShouldReturnCount_WhenCategoriesPresent() {
        when(categoryRepository.count()).thenReturn(5L);

        Long result = categoryService.getCategoryCount();

        assertEquals(5L, result);
        verify(categoryRepository, times(1)).count();
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenCategoryExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(modelMapper.map(mockCategory, CategoryResponse.class)).thenReturn(mockCategoryResponse);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());

        verify(categoryRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(mockCategory, CategoryResponse.class);
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(99L));
        assertTrue(exception.getMessage().contains("Category not found with id: 99"));

        verify(categoryRepository, times(1)).findById(99L);
        verify(modelMapper, never()).map(any(Category.class), eq(CategoryResponse.class));
    }

    @Test
    void createCategory_ShouldReturnCategory_WhenCategoryIsCreated() {
        Category mappedCategory = new Category();
        mappedCategory.setCategoryName("Electronics");
        mappedCategory.setDescription("Electronic products");
        mappedCategory.setImageUrl("electronics.jpg");

        when(modelMapper.map(mockCategoryRequest, Category.class)).thenReturn(mappedCategory);
        when(categoryRepository.save(mappedCategory)).thenReturn(mockCategory);
        when(modelMapper.map(mockCategory, CategoryResponse.class)).thenReturn(mockCategoryResponse);

        CategoryResponse result = categoryService.createCategory(mockCategoryRequest);

        assertNotNull(result);
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());

        verify(modelMapper, times(1)).map(mockCategoryRequest, Category.class);
        verify(categoryRepository, times(1)).save(mappedCategory);
        verify(modelMapper, times(1)).map(mockCategory, CategoryResponse.class);
    }

    @Test
    void updateCategory_ShouldReturnCategory_WhenCategoryExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        doNothing().when(modelMapper).map(mockCategoryRequest, mockCategory);
        when(categoryRepository.save(mockCategory)).thenReturn(mockCategory);
        when(modelMapper.map(mockCategory, CategoryResponse.class)).thenReturn(mockCategoryResponse);

        CategoryResponse result = categoryService.updateCategory(1L, mockCategoryRequest);

        assertNotNull(result);
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());

        verify(categoryRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(mockCategoryRequest, mockCategory);
        verify(categoryRepository, times(1)).save(mockCategory);
        verify(modelMapper, times(1)).map(mockCategory, CategoryResponse.class);
    }

    @Test
    void updateCategory_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(99L, mockCategoryRequest));
        assertTrue(exception.getMessage().contains("Category not found with id: 99"));

        verify(categoryRepository, times(1)).findById(99L);
        verify(modelMapper, never()).map(mockCategoryRequest, mockCategory);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDelete_WhenCategoryExists() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(99L));
        assertTrue(exception.getMessage().contains("Category not found with id: 99"));

        verify(categoryRepository, times(1)).existsById(99L);
        verify(categoryRepository, never()).deleteById(any(Long.class));
    }
}


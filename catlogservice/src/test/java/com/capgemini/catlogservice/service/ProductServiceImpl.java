package com.capgemini.catlogservice.service;

import com.capgemini.catlogservice.dto.ProductResponse;
import com.capgemini.catlogservice.entity.Category;
import com.capgemini.catlogservice.entity.Product;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.repository.ProductRepository;
import com.capgemini.catlogservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category mockCategory;
    private Product mockProduct;
    private ProductResponse mockProductResponse;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setCategoryId(1L);
        mockCategory.setCategoryName("Electronics");

        mockProduct = new Product();
        mockProduct.setProductId(1L);
        mockProduct.setProductName("iPhone 15");
        mockProduct.setProductDescription("Latest iPhone");
        mockProduct.setProductPrice(new BigDecimal("79999"));
        mockProduct.setProductStock(50);
        mockProduct.setImageUrl("iphone15.jpg");
        mockProduct.setBrand("Apple");
        mockProduct.setFeatured(true);
        mockProduct.setIsAvailable(true);
        mockProduct.setCategory(mockCategory);

        mockProductResponse = ProductResponse.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Latest iPhone")
                .productPrice(new BigDecimal("79999"))
                .productStock(50)
                .imageUrl("iphone15.jpg")
                .brand("Apple")
                .isAvailable(true)
                .categoryId(1L)
                .categoryName("Electronics")
                .build();
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(modelMapper.map(mockProduct, ProductResponse.class)).thenReturn(mockProductResponse);

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals("iPhone 15", result.getProductName());
        assertEquals("Electronics", result.getCategoryName());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.getProductById(99L));
        assertTrue(exception.getMessage().contains("Product not found with id: 99"));

        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    void getAllProducts_ShouldReturnPage_WhenNoFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct), pageable, 1);

        when(productRepository.findByIsAvailableTrue(any(Pageable.class))).thenReturn(mockPage);
        when(modelMapper.map(mockProduct, ProductResponse.class)).thenReturn(mockProductResponse);

        Page<ProductResponse> result = productService.getAllProducts(
                null, null, 0, 10, "createdAt", "desc"
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getProductName());

        verify(productRepository, times(1)).findByIsAvailableTrue(any(Pageable.class));
    }

    @Test
    void getAllProducts_ShouldReturnPage_WhenSearchApplied() {
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct));

        when(productRepository.findByProductNameContainingIgnoreCaseAndIsAvailableTrue(
                eq("iphone"), any(Pageable.class)))
                .thenReturn(mockPage);

        when(modelMapper.map(mockProduct, ProductResponse.class))
                .thenReturn(mockProductResponse);

        Page<ProductResponse> result = productService.getAllProducts(
                "iphone", null, 0, 10, "createdAt", "desc"
        );

        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1))
                .findByProductNameContainingIgnoreCaseAndIsAvailableTrue(eq("iphone"), any(Pageable.class));
    }

    @Test
    void getAllProducts_ShouldReturnPage_WhenCategoryFilterApplied() {
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct));

        when(productRepository.findByCategoryCategoryIdAndIsAvailableTrue(
                eq(1L), any(Pageable.class)))
                .thenReturn(mockPage);

        when(modelMapper.map(mockProduct, ProductResponse.class))
                .thenReturn(mockProductResponse);

        Page<ProductResponse> result = productService.getAllProducts(
                null, 1L, 0, 10, "createdAt", "desc"
        );

        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1))
                .findByCategoryCategoryIdAndIsAvailableTrue(eq(1L), any(Pageable.class));
    }

    @Test
    void getAllProducts_ShouldReturnPage_WhenSearchAndCategoryApplied() {
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct));

        when(productRepository.findByProductNameContainingIgnoreCaseAndCategoryCategoryIdAndIsAvailableTrue(
                eq("iphone"), eq(1L), any(Pageable.class)))
                .thenReturn(mockPage);

        when(modelMapper.map(mockProduct, ProductResponse.class))
                .thenReturn(mockProductResponse);

        Page<ProductResponse> result = productService.getAllProducts(
                "iphone", 1L, 0, 10, "createdAt", "desc"
        );

        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1))
                .findByProductNameContainingIgnoreCaseAndCategoryCategoryIdAndIsAvailableTrue(
                        eq("iphone"), eq(1L), any(Pageable.class));
    }

    @Test
    void deleteProduct_ShouldSetActiveFalse_WhenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        productService.deleteProduct(1L);

        assertFalse(mockProduct.getIsAvailable());
        verify(productRepository, times(1)).save(mockProduct);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(99L));
        assertTrue(exception.getMessage().contains("Product not found with id: 99"));

        verify(productRepository, never()).save(any(Product.class));
    }
}
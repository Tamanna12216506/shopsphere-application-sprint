package com.capgemini.catlogservice.service.impl;


import com.capgemini.catlogservice.dto.ProductRequest;
import com.capgemini.catlogservice.dto.ProductResponse;
import com.capgemini.catlogservice.entity.Category;
import com.capgemini.catlogservice.entity.Product;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.repository.ProductRepository;
import com.capgemini.catlogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<ProductResponse> getAllProducts(String search, Long categoryId, int page, int size, String sortBy, String sortDir) {
        //Create Sorting
        Sort sort = sortDir.equalsIgnoreCase("desc")? Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();

        //create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products;

        /// apply filters
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasCategory = categoryId != null;
        if(hasCategory && hasSearch) {
            products =productRepository.findByProductNameContainingIgnoreCaseAndCategoryCategoryIdAndIsAvailableTrue(search, categoryId, pageable);
        }else if(hasCategory) {
            products = productRepository.findByCategoryCategoryIdAndIsAvailableTrue(categoryId, pageable);
        }else if(hasSearch) {
            products = productRepository.findByProductNameContainingIgnoreCaseAndIsAvailableTrue(search, pageable);
        }else{
            products = productRepository.findByIsAvailableTrue(pageable);
        }
        return products.map(product -> modelMapper.map(product, ProductResponse.class));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()->new RuntimeException("Product not found with id: "+id));
        return modelMapper.map(product, ProductResponse.class);
    }

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product  product = modelMapper.map(productRequest, Product.class);
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(()->new RuntimeException("Category not found with id: "+productRequest.getCategoryId()));
        product.setCategory(category);
        product.setIsAvailable(true);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductResponse.class);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id).orElseThrow(()->new RuntimeException("Product not found with id: "+id));
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(()->new RuntimeException("Category not found with id: "+productRequest.getCategoryId()));
        product.setCategory(category);
        modelMapper.map(productRequest, product);
        Product updatedProduct = productRepository.save(product);
        return modelMapper.map(updatedProduct, ProductResponse.class);

    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()->new RuntimeException("Product not found with id: "+id));
        product.setIsAvailable(false);
        /// instead of directly deleting product from the db-marking it as inactive
        // Using soft delete (active flag) instead of hard delete to preserve data integrity and history
        productRepository.save(product);

    }

    public ProductResponse markAsFeatured(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setFeatured(true);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductResponse.class);
    }
}

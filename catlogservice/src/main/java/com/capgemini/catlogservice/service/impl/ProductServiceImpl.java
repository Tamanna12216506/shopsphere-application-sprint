package com.capgemini.catlogservice.service.impl;


import com.capgemini.catlogservice.config.RabbitMQConfig;
import com.capgemini.catlogservice.dto.ProductRequest;
import com.capgemini.catlogservice.dto.ProductResponse;
import com.capgemini.catlogservice.entity.Category;
import com.capgemini.catlogservice.entity.Product;
import com.capgemini.catlogservice.exception.BadRequestException;
import com.capgemini.catlogservice.exception.ResourceNotFoundException;
import com.capgemini.catlogservice.messaging.publisher.StockEventPublisher;
import com.capgemini.catlogservice.repository.CategoryRepository;
import com.capgemini.catlogservice.repository.ProductRepository;
import com.capgemini.catlogservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final StockEventPublisher stockEventPublisher;

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
        return products.map(this::toProductResponse);
    }

    @Cacheable(value = "availableProducts")
    @Override
    public List<ProductResponse> getAllAvailableProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long getAvailableProductCount() {
        return productRepository.countByIsAvailableTrue();
    }

    @Cacheable(value = "products", key = "#id")
    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product not found for id={}", id);
            return new ResourceNotFoundException("Product not found with id: " + id);
        });
        return toProductResponse(product);
    }

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product name={}, categoryId={}", productRequest.getProductName(), productRequest.getCategoryId());
        Product  product = modelMapper.map(productRequest, Product.class);
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(() -> {
            log.warn("Cannot create product. Category not found for id={}", productRequest.getCategoryId());
            return new ResourceNotFoundException("Category not found with id: " + productRequest.getCategoryId());
        });
        product.setCategory(category);
        product.setIsAvailable(true);
        Product savedProduct = productRepository.save(product);
        log.info("Product created with id={}", savedProduct.getProductId());
        return toProductResponse(savedProduct);
    }

    @Cacheable(value = {"products", "availableProducts"}, key = "#id")
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        log.info("Updating product id={}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Cannot update. Product not found for id={}", id);
            return new ResourceNotFoundException("Product not found with id: " + id);
        });
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(() -> {
            log.warn("Cannot update product id={}. Category not found for id={}", id, productRequest.getCategoryId());
            return new ResourceNotFoundException("Category not found with id: " + productRequest.getCategoryId());
        });
        // ✅ manual mapping (safe)
        product.setProductName(productRequest.getProductName());
        product.setProductDescription(productRequest.getProductDescription());
        product.setProductPrice(productRequest.getProductPrice());
        product.setProductStock(productRequest.getProductStock());
        product.setBrand(productRequest.getBrand());
        product.setImageUrl(productRequest.getImageUrl());

        product.setCategory(category);
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated id={}", updatedProduct.getProductId());
        return toProductResponse(updatedProduct);

    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Soft deleting product id={}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Cannot delete. Product not found for id={}", id);
            return new ResourceNotFoundException("Product not found with id: " + id);
        });
        product.setIsAvailable(false);
        /// instead of directly deleting product from the db-marking it as inactive
        // Using soft delete (active flag) instead of hard delete to preserve data integrity and history
        productRepository.save(product);
        log.info("Product marked unavailable id={}", id);

    }

    public ProductResponse markAsFeatured(Long id) {
        log.info("Marking product as featured id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot mark featured. Product not found for id={}", id);
                    return new ResourceNotFoundException("Product not found");
                });

        product.setFeatured(true);

        Product savedProduct = productRepository.save(product);
        log.info("Product marked as featured id={}", savedProduct.getProductId());

        return toProductResponse(savedProduct);
    }

    @Override
    public void reduceStock(Long productId, Integer quantity) {
        log.info("Reducing stock for productId={} by quantity={}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Cannot reduce stock. Product not found for id={}", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        // Check sufficient stock
        if (product.getProductStock() < quantity) {
            log.warn("Insufficient stock for productId={}, available={}, requested={}", productId, product.getProductStock(), quantity);
            throw new BadRequestException("Insufficient stock for product: "
                    + product.getProductName()
                    + ". Available: " + product.getProductStock()
                    + ", Requested: " + quantity);
        }

        // Reduce stock
        product.setProductStock(product.getProductStock() - quantity);
        productRepository.save(product);
        log.info("Stock reduced for productId={}, remainingStock={}", productId, product.getProductStock());
        // Publish low stock alert if stock drops below threshold
        if (product.getProductStock()
                < RabbitMQConfig.LOW_STOCK_THRESHOLD) {
            log.warn("Low stock alert triggered for productId={}, stock={}", product.getProductId(), product.getProductStock());
            stockEventPublisher.publishLowStockAlert(
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductStock()
            );
        }
    }

//    @Override
//    public ProductResponse getProductWithStockCheck(Long productId, Integer requiredQuantity) {
//        return null;
//    }
@Override
public void increaseStock(Long productId, Integer quantity) {
    log.info("Increasing stock for productId={} by quantity={}", productId, quantity);
    // Used when order is CANCELLED - return stock back
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                log.warn("Cannot increase stock. Product not found for id={}", productId);
                return new ResourceNotFoundException("Product not found with id: " + productId);
            });

    product.setProductStock(product.getProductStock() + quantity);
    productRepository.save(product);
    log.info("Stock increased for productId={}, newStock={}", productId, product.getProductStock());
}

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setProductDescription(product.getProductDescription());
        response.setProductPrice(product.getProductPrice());
        response.setProductStock(product.getProductStock());
        response.setBrand(product.getBrand());
        response.setImageUrl(product.getImageUrl());
        response.setCreatedAt(product.getCreatedAt());
        response.setAvailable(Boolean.TRUE.equals(product.getIsAvailable()));
        if (product.getCategory() != null && Hibernate.isInitialized(product.getCategory())) {
            response.setCategoryId(product.getCategory().getCategoryId());
            response.setCategoryName(product.getCategory().getCategoryName());
        }
        return response;
    }
}

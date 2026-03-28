package com.capgemini.catlogservice.repository;

import com.capgemini.catlogservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /*
     * Why using Page instead of List?
     *
     * In real-world applications, the number of products can be very large.
     * Fetching all records at once can cause performance issues and high memory usage.
     * Page<T> helps implement pagination and also provides metadata like
     * total pages, total elements, current page, etc.
     */

    /*
     * Why using ActiveTrue?
     *
     * active = true means the product is currently available/visible to users.
     * Instead of permanently deleting products from the database, we usually
     * mark them inactive (soft delete approach).
     *
     * Benefits:
     * - Keeps old product data safe
     * - Maintains order/history references
     * - Helps in auditing and reporting
     * - Prevents inactive/discontinued products from being shown to users
     *
     * So, most user-facing queries include ActiveTrue to return only active products.
     */

    // Search active products by name (case-insensitive, partial match)
    Page<Product> findByProductNameContainingIgnoreCaseAndIsAvailableTrue(String name, Pageable pageable);

    // Filter active products by category
    Page<Product> findByCategoryCategoryIdAndIsAvailableTrue(Long categoryId, Pageable pageable);

    // Search active products by name + category together
    Page<Product> findByProductNameContainingIgnoreCaseAndCategoryCategoryIdAndIsAvailableTrue(
            String name, Long categoryId, Pageable pageable);

    // Fetch all active products with pagination
    Page<Product> findByIsAvailableTrue(Pageable pageable);

    // Fetch all active products without pagination (for admin dashboard sync)
    List<Product> findByIsAvailableTrue();

    // Count only active products to match soft-delete behavior
    Long countByIsAvailableTrue();

    // Fetch featured and active products for homepage display
    List<Product> findByFeaturedTrueAndIsAvailableTrue();

}
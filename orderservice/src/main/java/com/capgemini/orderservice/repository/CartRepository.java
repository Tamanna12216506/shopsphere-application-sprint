package com.capgemini.orderservice.repository;

import com.capgemini.orderservice.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    //one user =one cart
    Optional<Cart> findByUserId(String userId);

    // check if user hast cart already
    boolean existsByUserId(String userId);
}

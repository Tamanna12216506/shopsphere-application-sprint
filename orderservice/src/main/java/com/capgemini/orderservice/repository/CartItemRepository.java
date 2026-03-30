package com.capgemini.orderservice.repository;

import com.capgemini.orderservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    //check if product already in cart
    //it used to increase quantity of product instead adding duplicate
    Optional<CartItem> findByCart_CartIdAndProductId(Long cartId, Long productId);
}

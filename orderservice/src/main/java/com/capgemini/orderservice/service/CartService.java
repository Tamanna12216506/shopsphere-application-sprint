package com.capgemini.orderservice.service;

import com.capgemini.orderservice.dto.AddToCartRequest;
import com.capgemini.orderservice.dto.CartDTO;
import com.capgemini.orderservice.dto.UpdateCartItemRequest;

public interface CartService {
    CartDTO addToCart(String userId, AddToCartRequest request);

    CartDTO getCart(String userId);

    CartDTO updateCartItem(String userId, Long cartItemId, UpdateCartItemRequest request);

    void removeCartItem(String userId, Long cartItemId);

    void clearCart(String userId);

}

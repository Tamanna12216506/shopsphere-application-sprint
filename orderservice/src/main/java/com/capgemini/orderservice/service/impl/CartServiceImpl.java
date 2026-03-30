package com.capgemini.orderservice.service.impl;

import com.capgemini.orderservice.client.CatalogClient;
import com.capgemini.orderservice.dto.*;
import com.capgemini.orderservice.entity.Cart;
import com.capgemini.orderservice.entity.CartItem;
import com.capgemini.orderservice.exception.BadRequestException;
import com.capgemini.orderservice.exception.ResourceNotFoundException;
import com.capgemini.orderservice.repository.CartItemRepository;
import com.capgemini.orderservice.repository.CartRepository;
import com.capgemini.orderservice.service.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional   //ensure database transactions - either all succeed or all fail
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    private final CatalogClient catalogClient; //to fetch product details for validation and trusted data

    /**
     * Adds a product to the user's cart. If the user has no cart yet, a new one is created.
     * If the product already exists in the cart, its quantity is incremented.
     */

    // Opens the circuit if catalog calls keep failing.
    @CircuitBreaker(name = "catalogService", fallbackMethod = "addToCartFallback")
    // Retries brief catalog issues before failing the request.
    @Retry(name = "catalogService")
    @Override
    public CartDTO addToCart(String userId, AddToCartRequest request) {
        // 1. Fetch product from catalog service
        ApiResponse<ProductResponse> apiResponse = catalogClient.getProductById(request.getProductId());

        ProductResponse product = apiResponse.getData();
        if (product == null) {
            log.warn("Product lookup failed in catalog: productId={}", request.getProductId());
            throw new RuntimeException("Product not found with id: " + request.getProductId());
        }

        // 2. Validate product availability
        if (product.getAvailable() == null || !product.getAvailable()) {
            log.warn("Product unavailable: productId={}", request.getProductId());
            throw new RuntimeException("Product is currently unavailable");
        }

        // 3. Validate price
        if (product.getProductPrice() == null || product.getProductPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid product price from catalog: productId={}, price={}", request.getProductId(), product.getProductPrice());
            throw new RuntimeException("Invalid product price from catalog service");
        }

        // 4. Validate product name
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            log.warn("Invalid product name from catalog: productId={}", request.getProductId());
            throw new RuntimeException("Invalid product name from catalog service");
        }

        // 5. Validate image URL
        String imageUrl = product.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "default-image.png"; // or keep null based on your requirement
        }

        // 6. Validate stock
        if (product.getProductStock() == null || product.getProductStock() < request.getQuantity()) {
            log.warn("Insufficient stock: productId={}, requested={}, available={}", request.getProductId(), request.getQuantity(), product.getProductStock());
            throw new RuntimeException("Insufficient stock available");
        }

        // 7. Get existing cart or create new one
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().userId(userId).build();
            return cartRepository.save(newCart);
        });

        // 8. Check if product already exists in cart
        cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), request.getProductId())
                .ifPresentOrElse(
                        existingItem -> {
                            int newQuantity = existingItem.getQuantity() + request.getQuantity();

                            if (product.getProductStock() < newQuantity) {
                                log.warn("Requested quantity exceeds stock: userId={}, productId={}, requestedTotal={}, available={}", userId, request.getProductId(), newQuantity, product.getProductStock());
                                throw new RuntimeException("Requested total quantity exceeds available stock");
                            }

                            existingItem.setQuantity(newQuantity);

                            // always sync trusted values from catalog
                            existingItem.setProductName(product.getProductName());
                            existingItem.setProductImageUrl(product.getImageUrl());
                            existingItem.setPrice(product.getProductPrice());

                            cartItemRepository.save(existingItem);
                            log.info("Updated cart item quantity: userId={}, cartItemId={}, productId={}, quantity={}", userId, existingItem.getCartItemId(), existingItem.getProductId(), existingItem.getQuantity());
                        },
                        () -> {
                            CartItem cartItem = CartItem.builder()
                                    .cart(cart)
                                    .productId(product.getProductId())
                                    .productName(product.getProductName())
                                    .productImageUrl(product.getImageUrl())
                                    .price(product.getProductPrice())
                                    .quantity(request.getQuantity())
                                    .build();

                            cart.getItems().add(cartItem);
                            cartItemRepository.save(cartItem);
                            log.info("Added new cart item: userId={}, productId={}, quantity={}", userId, cartItem.getProductId(), cartItem.getQuantity());
                        }
                );
        return getCart(userId);
        //        // Reusing getCart() to avoid duplicate mapping logic and ensure consistent cart response with correct totals

    }

    private CartDTO addToCartFallback(String userId, AddToCartRequest request, Throwable throwable) {
        log.error("Catalog fallback triggered while adding to cart for productId={}: {}", request.getProductId(), throwable.getMessage());
        throw new BadRequestException("Catalog service is temporarily unavailable");
    }
    /**
     * Fetches the user's cart and computes derived values like item subtotal, total items,
     * and total price for the API response.
     */
    @Override
    @Transactional(readOnly=true)
    public CartDTO getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(()->{
            Cart newCart = Cart.builder().userId(userId).build();
            return cartRepository.save(newCart);
        });
        cart.getItems().size(); //force loading of lazy items for mapping

    //ModelMapper maps: Cart entity → CartDTO response, including mapping of nested CartItem entities to CartItemDTOs
//        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
   CartDTO cartDTO = new CartDTO();
   cartDTO.setCartId(cart.getCartId());
   cartDTO.setUserId(cart.getUserId());
    //Map items manually because subtotal needs calculation
        List<CartItemDTO> itemDTOs = cart.getItems().stream().map(item -> {
            CartItemDTO itemDTO = modelMapper.map(item, CartItemDTO.class);
            itemDTO.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDTO;
        }).toList();

        BigDecimal totalAmount = itemDTOs.stream().map(CartItemDTO::getSubtotal)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemDTOs.stream().mapToInt(CartItemDTO::getQuantity).sum();

        cartDTO.setItems(itemDTOs);
        cartDTO.setTotalItems(totalItems);
        cartDTO.setTotalPrice(totalAmount);
        return cartDTO;
    }

    /**
     * Updates quantity for a specific cart item after validating cart ownership.
     */
    @Override
    public CartDTO updateCartItem(String userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(()-> new ResourceNotFoundException("Cart not found for user: "+userId));
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(()-> new ResourceNotFoundException("Cart item not found with id: "+cartItemId));

        if(!item.getCart().getCartId().equals(cart.getCartId())){
            log.warn("Cart ownership mismatch while updating item: userId={}, cartItemId={}", userId, cartItemId);
            throw new BadRequestException("Cart item id not match");
        }
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        log.info("Cart item updated: userId={}, cartItemId={}, quantity={}", userId, cartItemId, item.getQuantity());
        return getCart(userId);
    }

    /**
     * Removes a specific item from the user's cart after validating ownership.
     */
    @Override
    public void removeCartItem(String userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(()-> new ResourceNotFoundException("Cart not found for user: "+userId));
        CartItem item  =   cartItemRepository.findById(cartItemId).orElseThrow(()-> new ResourceNotFoundException("Cart item not found with id: "+cartItemId));
        if(!item.getCart().getCartId().equals(cart.getCartId())){
            log.warn("Cart ownership mismatch while removing item: userId={}, cartItemId={}", userId, cartItemId);
            throw new BadRequestException("Cart item id not match");
        }
        cartItemRepository.delete(item);
        log.info("Cart item removed: userId={}, cartItemId={}", userId, cartItemId);

    }

    /**
     * Removes all items from the user's cart.
     */
    @Override
    public void clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(()-> new ResourceNotFoundException("Cart not found for user: "+userId));
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared for userId={}", userId);

    }
}

package com.capgemini.orderservice.controller;

import com.capgemini.orderservice.dto.AddToCartRequest;
import com.capgemini.orderservice.dto.ApiResponse;
import com.capgemini.orderservice.dto.CartDTO;
import com.capgemini.orderservice.dto.UpdateCartItemRequest;
import com.capgemini.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/cart")
public class CartController {
    private final CartService cartService;

    //add product to cart
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(@RequestHeader("X-Authenticated-User") String userId,@Valid @RequestBody AddToCartRequest request) {
        CartDTO cartDTO = cartService.addToCart(userId, request);
        ApiResponse<CartDTO> apiResponse = ApiResponse.<CartDTO>builder()
                .status(200).message("Item added to cart successfully").data(cartDTO).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Fetches complete cart of logged-in user
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@RequestHeader("X-Authenticated-User") String userId) {

        CartDTO cartDTO = cartService.getCart(userId);

        return ResponseEntity.ok(ApiResponse.<CartDTO>builder()
                        .status(200)
                        .message("Cart fetched successfully")
                        .data(cartDTO).build()
        );
    }

    // Updates quantity of a specific cart item and returns updated cart
    @PutMapping("/items/{id}") // this id is of cartItem
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(@RequestHeader("X-Authenticated-User") String userId,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody UpdateCartItemRequest request) {
        CartDTO cartDTO = cartService.updateCartItem(userId, id, request);

        return ResponseEntity.ok(ApiResponse.<CartDTO>builder()
                        .status(200)
                        .message("Cart item updated successfully")
                        .data(cartDTO).build()
        );
    }

    // Removes a specific item from cart
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<String>> removeCartItem(@RequestHeader("X-Authenticated-User") String userId, @PathVariable Long id) {

        cartService.removeCartItem(userId, id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                        .status(200)
                        .message("Item removed from cart successfully")
                        .data(null).build()
        );
    }

    // Clears all items from user's cart
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> clearCart(@RequestHeader("X-Authenticated-User") String userId) {

        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                        .status(200)
                        .message("Cart cleared successfully")
                        .data(null).build()
        );
    }
}

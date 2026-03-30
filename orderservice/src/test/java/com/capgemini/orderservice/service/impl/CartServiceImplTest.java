package com.capgemini.orderservice.service.impl;

import com.capgemini.orderservice.client.CatalogClient;
import com.capgemini.orderservice.dto.AddToCartRequest;
import com.capgemini.orderservice.dto.ApiResponse;
import com.capgemini.orderservice.dto.CartDTO;
import com.capgemini.orderservice.dto.CartItemDTO;
import com.capgemini.orderservice.dto.ProductResponse;
import com.capgemini.orderservice.dto.UpdateCartItemRequest;
import com.capgemini.orderservice.entity.Cart;
import com.capgemini.orderservice.entity.CartItem;
import com.capgemini.orderservice.exception.BadRequestException;
import com.capgemini.orderservice.exception.ResourceNotFoundException;
import com.capgemini.orderservice.repository.CartItemRepository;
import com.capgemini.orderservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final String USER_ID = "user1@example.com";

    private Cart cart;
    private CartItem cartItem;
    private ProductResponse productResponse;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .cartId(1L)
                .userId(USER_ID)
                .items(new ArrayList<>())
                .build();

        cartItem = CartItem.builder()
                .CartItemId(11L)
                .cart(cart)
                .productId(101L)
                .productName("iPhone 15")
                .productImageUrl("iphone.jpg")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .build();

        productResponse = new ProductResponse();
        productResponse.setProductId(101L);
        productResponse.setProductName("iPhone 15");
        productResponse.setProductPrice(new BigDecimal("1000"));
        productResponse.setProductStock(20);
        productResponse.setImageUrl("iphone.jpg");
        productResponse.setAvailable(true);

        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setProductId(101L);
        addToCartRequest.setQuantity(2);
    }

    @Test
    void addToCart_ShouldCreateNewCartAndItem_WhenCartNotExistsAndItemNotExists() {
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder().data(productResponse).build();
        CartItemDTO cartItemDTO = CartItemDTO.builder()
                .productId(101L)
                .productName("iPhone 15")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .build();

        when(catalogClient.getProductById(101L)).thenReturn(apiResponse);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty(), Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCart_CartIdAndProductId(1L, 101L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(CartItem.class), eq(CartItemDTO.class))).thenReturn(cartItemDTO);

        CartDTO result = cartService.addToCart(USER_ID, addToCartRequest);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertEquals(2, result.getTotalItems());
        assertEquals(new BigDecimal("2000"), result.getTotalPrice());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ShouldIncreaseQuantity_WhenItemAlreadyExists() {
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder().data(productResponse).build();
        CartItem existingItem = CartItem.builder()
                .CartItemId(15L)
                .cart(cart)
                .productId(101L)
                .productName("Old Name")
                .price(new BigDecimal("900"))
                .quantity(1)
                .build();
        cart.getItems().add(existingItem);

        CartItemDTO cartItemDTO = CartItemDTO.builder()
                .productId(101L)
                .productName("iPhone 15")
                .price(new BigDecimal("1000"))
                .quantity(3)
                .build();

        when(catalogClient.getProductById(101L)).thenReturn(apiResponse);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartIdAndProductId(1L, 101L)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
        when(modelMapper.map(existingItem, CartItemDTO.class)).thenReturn(cartItemDTO);

        CartDTO result = cartService.addToCart(USER_ID, addToCartRequest);

        assertNotNull(result);
        assertEquals(3, existingItem.getQuantity());
        assertEquals("iPhone 15", existingItem.getProductName());
        assertEquals(new BigDecimal("3000"), result.getTotalPrice());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void addToCart_ShouldThrowException_WhenProductUnavailable() {
        productResponse.setAvailable(false);
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder().data(productResponse).build();
        when(catalogClient.getProductById(101L)).thenReturn(apiResponse);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(USER_ID, addToCartRequest));

        assertEquals("Product is currently unavailable", exception.getMessage());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCart_ShouldThrowException_WhenRequestedQuantityExceedsStock() {
        productResponse.setProductStock(1);
        ApiResponse<ProductResponse> apiResponse = ApiResponse.<ProductResponse>builder().data(productResponse).build();
        when(catalogClient.getProductById(101L)).thenReturn(apiResponse);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(USER_ID, addToCartRequest));

        assertEquals("Insufficient stock available", exception.getMessage());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCart_ShouldReturnExistingCartWithCalculatedTotals() {
        cart.getItems().add(cartItem);
        CartItemDTO itemDTO = CartItemDTO.builder()
                .cartItemId(11L)
                .productId(101L)
                .productName("iPhone 15")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(modelMapper.map(cartItem, CartItemDTO.class)).thenReturn(itemDTO);

        CartDTO result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getCartId());
        assertEquals(2, result.getTotalItems());
        assertEquals(new BigDecimal("2000"), result.getTotalPrice());
    }

    @Test
    void updateCartItem_ShouldUpdateQuantity_WhenOwnershipMatches() {
        cart.getItems().add(cartItem);
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        CartItemDTO itemDTO = CartItemDTO.builder()
                .cartItemId(11L)
                .productId(101L)
                .productName("iPhone 15")
                .price(new BigDecimal("1000"))
                .quantity(5)
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart), Optional.of(cart));
        when(cartItemRepository.findById(11L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(modelMapper.map(cartItem, CartItemDTO.class)).thenReturn(itemDTO);

        CartDTO result = cartService.updateCartItem(USER_ID, 11L, request);

        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());
        assertEquals(5, result.getTotalItems());
        assertEquals(new BigDecimal("5000"), result.getTotalPrice());
    }

    @Test
    void updateCartItem_ShouldThrowException_WhenOwnershipMismatch() {
        Cart otherCart = Cart.builder().cartId(2L).userId("other@example.com").items(new ArrayList<>()).build();
        cartItem.setCart(otherCart);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(11L)).thenReturn(Optional.of(cartItem));

        assertThrows(BadRequestException.class, () -> cartService.updateCartItem(USER_ID, 11L, request));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeCartItem_ShouldDeleteItem_WhenOwnershipMatches() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(11L)).thenReturn(Optional.of(cartItem));

        cartService.removeCartItem(USER_ID, 11L);

        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void clearCart_ShouldThrowException_WhenCartMissing() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.clearCart(USER_ID));
        verify(cartRepository, never()).save(any(Cart.class));
    }
}


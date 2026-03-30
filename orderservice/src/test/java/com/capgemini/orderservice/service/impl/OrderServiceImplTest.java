package com.capgemini.orderservice.service.impl;

import com.capgemini.orderservice.client.CatalogClient;
import com.capgemini.orderservice.client.PaymentClient;
import com.capgemini.orderservice.dto.CheckoutRequest;
import com.capgemini.orderservice.dto.DeliveryAddressDTO;
import com.capgemini.orderservice.dto.OrderDTO;
import com.capgemini.orderservice.dto.OrderItemDTO;
import com.capgemini.orderservice.dto.PaymentResponse;
import com.capgemini.orderservice.dto.RefundRequest;
import com.capgemini.orderservice.dto.StockUpdateRequest;
import com.capgemini.orderservice.dto.UpdateOrderStatusRequest;
import com.capgemini.orderservice.entity.Cart;
import com.capgemini.orderservice.entity.CartItem;
import com.capgemini.orderservice.entity.DeliveryAddress;
import com.capgemini.orderservice.entity.Order;
import com.capgemini.orderservice.entity.OrderItem;
import com.capgemini.orderservice.enums.OrderStatus;
import com.capgemini.orderservice.enums.PaymentMode;
import com.capgemini.orderservice.exception.BadRequestException;
import com.capgemini.orderservice.exception.ResourceNotFoundException;
import com.capgemini.orderservice.messaging.publisher.OrderEventPublisher;
import com.capgemini.orderservice.repository.CartRepository;
import com.capgemini.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final String USER_ID = "user1@example.com";

    private Cart cart;
    private CartItem cartItem;
    private CheckoutRequest checkoutRequest;
    private DeliveryAddressDTO deliveryAddressDTO;
    private DeliveryAddress deliveryAddress;
    private OrderItem mappedOrderItem;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .cartId(1L)
                .userId(USER_ID)
                .items(new ArrayList<>())
                .build();

        cartItem = CartItem.builder()
                .CartItemId(10L)
                .cart(cart)
                .productId(101L)
                .productName("iPhone 15")
                .productImageUrl("iphone.jpg")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .build();
        cart.getItems().add(cartItem);

        deliveryAddressDTO = DeliveryAddressDTO.builder()
                .fullName("John Doe")
                .phoneNumber("9999999999")
                .addressLine("Main street")
                .city("Pune")
                .state("MH")
                .pinCode("411001")
                .build();

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setDeliveryAddress(deliveryAddressDTO);
        checkoutRequest.setPaymentMode(PaymentMode.CARD);

        deliveryAddress = DeliveryAddress.builder()
                .fullName("John Doe")
                .phoneNumber("9999999999")
                .addressLine("Main street")
                .city("Pune")
                .state("MH")
                .pinCode("411001")
                .build();

        mappedOrderItem = OrderItem.builder()
                .productId(101L)
                .productName("iPhone 15")
                .productImageUrl("iphone.jpg")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .build();
    }

    @Test
    void checkout_ShouldConfirmOrderAndClearCart_WhenPaymentSucceeds() {
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .success(true)
                .transactionId("txn-123")
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(modelMapper.map(deliveryAddressDTO, DeliveryAddress.class)).thenReturn(deliveryAddress);
        when(modelMapper.map(cartItem, OrderItem.class)).thenReturn(mappedOrderItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getOrderId() == null) {
                order.setOrderId(200L);
            }
            return order;
        });
        when(paymentClient.processPayment(any())).thenReturn(paymentResponse);
        stubOrderMappingsWithItemsAndAddress();

        OrderDTO result = orderService.checkout(USER_ID, checkoutRequest);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getOrderStatus());
        assertEquals("txn-123", result.getPaymentId());
        verify(catalogClient, times(1)).reduceStock(eq(101L), any(StockUpdateRequest.class));
        verify(cartRepository, times(1)).save(cart);
        verify(orderEventPublisher, times(1)).publishOrderConfirmed(any());
    }

    @Test
    void checkout_ShouldMarkFailed_WhenPaymentFails() {
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .success(false)
                .message("declined")
                .build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(modelMapper.map(deliveryAddressDTO, DeliveryAddress.class)).thenReturn(deliveryAddress);
        when(modelMapper.map(cartItem, OrderItem.class)).thenReturn(mappedOrderItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getOrderId() == null) {
                order.setOrderId(201L);
            }
            return order;
        });
        when(paymentClient.processPayment(any())).thenReturn(paymentResponse);

        stubOrderMappingsWithItemsAndAddress();

        OrderDTO result = orderService.checkout(USER_ID, checkoutRequest);

        assertEquals(OrderStatus.FAILED, result.getOrderStatus());
        verify(catalogClient, never()).reduceStock(any(Long.class), any(StockUpdateRequest.class));
        verify(cartRepository, never()).save(cart);
        verify(orderEventPublisher, never()).publishOrderConfirmed(any());
    }

    @Test
    void checkout_ShouldThrowException_WhenCartNotFound() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.checkout(USER_ID, checkoutRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_ShouldThrowException_WhenUserDoesNotOwnOrder() {
        Order order = Order.builder().orderId(55L).userId("other@example.com").items(new ArrayList<>()).build();
        when(orderRepository.findById(55L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.getOrderById(USER_ID, 55L));
    }

    @Test
    void cancelOrder_ShouldCancelAndRefundAndRestoreStock_WhenConfirmedAndNonCod() {
        OrderItem orderItem = OrderItem.builder()
                .orderItemId(1L)
                .productId(101L)
                .productName("iPhone 15")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .subtotal(new BigDecimal("2000"))
                .build();

        Order order = Order.builder()
                .orderId(300L)
                .userId(USER_ID)
                .orderStatus(OrderStatus.CONFIRMED)
                .paymentMode(PaymentMode.CARD)
                .totalAmount(new BigDecimal("2000"))
                .deliveryAddress(deliveryAddress)
                .items(new ArrayList<>(List.of(orderItem)))
                .build();
        orderItem.setOrder(order);

        when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
        when(paymentClient.processRefund(any(RefundRequest.class))).thenReturn(PaymentResponse.builder().success(true).build());
        when(orderRepository.save(order)).thenReturn(order);

        stubOrderMappingsWithItemsAndAddress();

        OrderDTO result = orderService.cancelOrder(USER_ID, 300L);

        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        verify(paymentClient, times(1)).processRefund(any(RefundRequest.class));
        verify(catalogClient, times(1)).increaseStock(eq(101L), any(StockUpdateRequest.class));
        verify(orderEventPublisher, times(1)).publishOrderCancelled(any());
    }

    @Test
    void cancelOrder_ShouldCancelWithoutRefund_WhenConfirmedAndCod() {
        OrderItem orderItem = OrderItem.builder()
                .orderItemId(1L)
                .productId(101L)
                .productName("iPhone 15")
                .price(new BigDecimal("1000"))
                .quantity(2)
                .subtotal(new BigDecimal("2000"))
                .build();

        Order order = Order.builder()
                .orderId(301L)
                .userId(USER_ID)
                .orderStatus(OrderStatus.CONFIRMED)
                .paymentMode(PaymentMode.COD)
                .totalAmount(new BigDecimal("2000"))
                .deliveryAddress(deliveryAddress)
                .items(new ArrayList<>(List.of(orderItem)))
                .build();
        orderItem.setOrder(order);

        when(orderRepository.findById(301L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        stubOrderMappingsWithItemsAndAddress();

        OrderDTO result = orderService.cancelOrder(USER_ID, 301L);

        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        verify(paymentClient, never()).processRefund(any(RefundRequest.class));
        verify(catalogClient, times(1)).increaseStock(eq(101L), any(StockUpdateRequest.class));
        verify(orderEventPublisher, times(1)).publishOrderCancelled(any());
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderIsShipped() {
        Order order = Order.builder()
                .orderId(400L)
                .userId(USER_ID)
                .orderStatus(OrderStatus.SHIPPED)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(400L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(USER_ID, 400L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getMyOrders_ShouldReturnMappedOrders() {
        Order order = Order.builder()
                .orderId(500L)
                .userId(USER_ID)
                .orderStatus(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(order));
        stubOrderOnlyMapping();

        List<OrderDTO> result = orderService.getMyOrders(USER_ID);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getOrderStatus());
    }

    @Test
    void updateOrderStatus_ShouldUpdateAndReturnMappedOrder() {
        Order order = Order.builder()
                .orderId(600L)
                .userId(USER_ID)
                .orderStatus(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(600L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        stubOrderOnlyMapping();

        OrderDTO result = orderService.updateOrderStatus(600L, request);

        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());
        assertEquals(OrderStatus.SHIPPED, result.getOrderStatus());
    }

    @Test
    void getOrderByIdForAdmin_ShouldThrowException_WhenOrderMissing() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderByIdForAdmin(999L));
    }

    private void stubOrderOnlyMapping() {
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return OrderDTO.builder()
                    .userId(order.getUserId())
                    .orderStatus(order.getOrderStatus())
                    .paymentMode(order.getPaymentMode())
                    .paymentId(order.getPaymentId())
                    .totalAmount(order.getTotalAmount())
                    .build();
        });
    }

    private void stubOrderMappingsWithItemsAndAddress() {
        stubOrderOnlyMapping();
        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenAnswer(invocation -> {
            OrderItem item = invocation.getArgument(0);
            return OrderItemDTO.builder()
                    .orderItemId(item.getOrderItemId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .build();
        });

        when(modelMapper.map(any(DeliveryAddress.class), eq(DeliveryAddressDTO.class))).thenReturn(deliveryAddressDTO);
    }
}


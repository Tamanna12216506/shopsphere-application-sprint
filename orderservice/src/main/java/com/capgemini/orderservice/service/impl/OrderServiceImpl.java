package com.capgemini.orderservice.service.impl;

import com.capgemini.orderservice.client.CatalogClient;
import com.capgemini.orderservice.client.PaymentClient;
import com.capgemini.orderservice.dto.*;
import com.capgemini.orderservice.entity.Cart;
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
import com.capgemini.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final PaymentClient paymentClient;
    private final CatalogClient catalogClient;
    private final OrderEventPublisher orderEventPublisher;

    //-- checkout converts user's cart into order, saves address and payment mode, and creates final order
    @Override
    @Caching(evict = {@CacheEvict(value = "myOrders", key = "#userId"), @CacheEvict(value = "allOrders", key = "'all'"),
            @CacheEvict(value = "orderById", allEntries = true), @CacheEvict(value = "adminOrderById", allEntries = true)})
    public OrderDTO checkout(String userId, CheckoutRequest request) {
        // Fetch user's cart
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        //prevent checkout if cart is empty
        if(cart.getItems()==null || cart.getItems().isEmpty()){
            throw new BadRequestException("Cart not found for user: " + userId);
        }

        // Map delivery address DTO to entity
        DeliveryAddress deliveryAddress = modelMapper.map(request.getDeliveryAddress(), DeliveryAddress.class);

        // Convert cart item into order item using snapshot values
        List<OrderItem> orderItems = new ArrayList<>(cart.getItems().stream().map(cartItem -> {
            OrderItem orderItem = modelMapper.map(cartItem, OrderItem.class);
            BigDecimal subtotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setSubtotal(subtotal);
            return orderItem;
        }).toList()
        );

        //calculate total order amount
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order with initial PENDING status
        Order order= Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .paymentMode(request.getPaymentMode())
                .deliveryAddress(deliveryAddress)
                .build();

        // set parent order reference in order items
        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        // save order before calling payment before calling payment service
        Order savedOrder = orderRepository.save(order);

        // Call payment service to process payment
        PaymentResponse paymentResponse= processPayment(savedOrder,request.getPaymentMode());

        //update order status
        if(paymentResponse.isSuccess()){
            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            savedOrder.setPaymentId(resolvePaymentReference(paymentResponse));

            // Reduce stock in Catalog Service for each ordered item
            reduceStockForOrderItems(savedOrder.getItems());

            //clear cart after successful payment
            cart.getItems().clear();
            cartRepository.save(cart);
            // Publish ORDER_CONFIRMED event to RabbitMQ
            orderEventPublisher.publishOrderConfirmed(
                    OrderEventMessage.builder()
                            .orderId(savedOrder.getOrderId())
                            .userId(savedOrder.getUserId())
                            .status(OrderStatus.CONFIRMED)
                            .totalAmount(savedOrder.getTotalAmount())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
            log.info("Checkout confirmed for orderId={} userId={} paymentId={}", savedOrder.getOrderId(), savedOrder.getUserId(), savedOrder.getPaymentId());
        }else{
            savedOrder.setOrderStatus(OrderStatus.FAILED);
            log.warn("Checkout failed for orderId={} userId={}", savedOrder.getOrderId(), savedOrder.getUserId());
        }
        return toOrderDTO(orderRepository.save(savedOrder));
    }

    private PaymentResponse processPayment(Order savedOrder,PaymentMode paymentMode) {
        try {
            PaymentRequest paymentRequest= PaymentRequest.builder()
                    .orderId(savedOrder.getOrderId())
                    .userId(savedOrder.getUserId())
                    .amount(savedOrder.getTotalAmount())
                    .paymentMode(paymentMode)
                    .build();
            PaymentResponse response = paymentClient.processPayment(paymentRequest);
            return response;
        }catch (Exception e){
            log.error("Payment service call failed for orderId={}: {}", savedOrder.getOrderId(), e.getMessage());
            return PaymentResponse.builder()
                    .success(false)
                    .message("Payment service error: "+e.getMessage())
                    .build();
        }
    }

    //convert order entity to order DTO
    private OrderDTO toOrderDTO(Order save) {
        //Map basics order fields
        OrderDTO orderDTO = modelMapper.map(save, OrderDTO.class);

        // Map order items
        List<OrderItemDTO> itemDTOs = save.getItems().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .toList();

        //Map delivery address
        DeliveryAddressDTO deliveryAddressDTO = null;
        if(save.getDeliveryAddress() != null){
            deliveryAddressDTO = modelMapper.map(save.getDeliveryAddress(), DeliveryAddressDTO.class);
        }
        orderDTO.setOrderItems(itemDTOs);
        orderDTO.setDeliveryAddress(deliveryAddressDTO);
        return orderDTO;
    }

    // Reduce stock in Catalog Service for each item.
    private void reduceStockForOrderItems(List<OrderItem> items) {
        items.forEach(item -> {
            try {
                catalogClient.reduceStock(item.getProductId(), new StockUpdateRequest(item.getQuantity()));
            } catch (Exception e) {
                // Log error but don't fail the order
                // Stock sync can be fixed manually or via scheduled job
                log.error("Failed to reduce stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        });
    }
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orderById", key = "#userId + ':' + #orderId")
    public OrderDTO getOrderById(String userId, Long orderId) {
        // fetching order by id
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));

        //ensuring user can only view their own order
        if(!order.getUserId().equals(userId)){
            throw new BadRequestException("Order id not match");
        }
        return toOrderDTO(order);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "myOrders", key = "#userId")
    public List<OrderDTO> getMyOrders(String userId) {
        //return all order of logged-in user
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderDTO)
                .toList();
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "myOrders", key = "#userId"), @CacheEvict(value = "allOrders", key = "'all'"),
            @CacheEvict(value = "orderById", allEntries = true), @CacheEvict(value = "adminOrderById", allEntries = true)})
    public OrderDTO cancelOrder(String userId, Long orderId) {
        log.info("Cancelling orderId={} for userId={}", orderId, userId);
        //fetch user
        Order  order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));
        if(!order.getUserId().equals(userId)){
            throw new BadRequestException("Order id not match");
        }

        //prevention cancellation after shipping stages
        if(order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED){
            throw new BadRequestException("Order cannot be cancelled at this stage");
        }
        OrderStatus previousStatus = order.getOrderStatus();

        order.setOrderStatus(OrderStatus.CANCELLED);

        // If order was CONFIRMED -> refund payment first and then restore stock
        if (previousStatus == OrderStatus.CONFIRMED) {
            refundPaymentIfRequired(order);
            restoreStockForOrderItems(order.getItems());
        }

        // Publish ORDER_CANCELLED event to RabbitMQ
        orderEventPublisher.publishOrderCancelled(
                OrderEventMessage.builder()
                        .orderId(order.getOrderId())
                        .userId(order.getUserId())
                        .status(OrderStatus.CANCELLED)
                        .totalAmount(order.getTotalAmount())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
        orderRepository.save(order);
        log.info("Order cancelled orderId={} userId={}", order.getOrderId(), order.getUserId());
        return toOrderDTO(order);
    }

    private void restoreStockForOrderItems(List<OrderItem> items) {
        items.forEach(item -> {
            try {
                catalogClient.increaseStock(
                        item.getProductId(),
                        new StockUpdateRequest(item.getQuantity())
                );
            } catch (Exception e) {
                log.error("Failed to restore stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        });
    }

    private String resolvePaymentReference(PaymentResponse paymentResponse) {
        if (paymentResponse.getTransactionId() != null && !paymentResponse.getTransactionId().isBlank()) {
            return paymentResponse.getTransactionId();
        }
        return paymentResponse.getPaymentId();
    }

    private void refundPaymentIfRequired(Order order) {
        if (order.getPaymentMode() == PaymentMode.COD) {
            return;
        }
        try {
            paymentClient.processRefund(RefundRequest.builder()
                    .orderId(order.getOrderId())
                    .reason("Order cancelled by user")
                    .build());
        } catch (Exception e) {
            log.error("Payment refund failed for orderId={}: {}", order.getOrderId(), e.getMessage());
            throw new BadRequestException("Unable to cancel order because payment refund failed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allOrders", key = "'all'")
    public List<OrderDTO> getAllOrders() {
        //admin fetch all order
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toOrderDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "adminOrderById", key = "#orderId")
    public OrderDTO getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));
        return toOrderDTO(order);
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "allOrders", key = "'all'"), @CacheEvict(value = "myOrders", allEntries = true),
            @CacheEvict(value = "orderById", allEntries = true), @CacheEvict(value = "adminOrderById", allEntries = true)})
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order status for orderId={} to status={}", orderId, request.getStatus());
        //admin update order status
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));
        order.setOrderStatus(request.getStatus());
        return toOrderDTO(orderRepository.save(order));
    }
}

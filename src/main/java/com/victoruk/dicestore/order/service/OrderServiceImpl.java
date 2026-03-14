package com.victoruk.dicestore.order.service;

import com.victoruk.dicestore.cart.entity.CartItem;
import com.victoruk.dicestore.common.exception.InsufficientStockException;
import com.victoruk.dicestore.common.exception.OrderCancellationNotAllowedException;
import com.victoruk.dicestore.common.exception.ResourceNotFoundException;
import com.victoruk.dicestore.common.security.AuthenticatedUserResolver;
import com.victoruk.dicestore.order.dto.OrderItemReponseDto;
import com.victoruk.dicestore.order.dto.OrderResponseDto;
import com.victoruk.dicestore.cart.repository.CartItemRepository;
import com.victoruk.dicestore.order.entity.Order;
import com.victoruk.dicestore.order.entity.OrderItem;
import com.victoruk.dicestore.order.entity.OrderStatus;
import com.victoruk.dicestore.order.repository.OrderRepository;
import com.victoruk.dicestore.product.entity.Product;
import com.victoruk.dicestore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthenticatedUserResolver userResolver;

    @Override
    @Transactional
    public void createOrder() {
        User user = userResolver.getAuthenticatedUser();
        log.info("Starting order creation for customer [{}]", user.getEmail());

        // Fetch cart items
        List<CartItem> cartItems = cartItemRepository.findByCartUser(user);
        if (cartItems.isEmpty()) {
            log.warn("Customer [{}] attempted to create order with empty cart", user.getEmail());
            throw new RuntimeException("Cart is empty");
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT); // ← fixed from ApplicationConstants string

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();

            // Stock check — guard against overselling
            if (product.getStock() < cartItem.getQuantity()) {
                log.warn("Insufficient stock for product [{}]. Available: {}, Requested: {}",
                        product.getName(), product.getStock(), cartItem.getQuantity());
                throw new InsufficientStockException(product.getName());
            }

            // Deduct stock — optimistic locking on Product handles concurrent updates
            product.setStock(product.getStock() - cartItem.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice()); // use stored cart price — respects discounts
            log.debug("Adding product [{}] x [{}] to order", product.getName(), cartItem.getQuantity());
            return item;
        }).collect(Collectors.toList());

        // Calculate total
        BigDecimal total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setOrderItems(orderItems);
        order.setTotalPrice(total);

        orderRepository.save(order);
        log.info("Order [{}] created successfully for customer [{}] with total [{}]",
                order.getOrderId(), user.getEmail(), total);

        // Cart is NOT cleared here — only cleared after confirmed payment via webhook
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getCustomerOrders() {
        User user = userResolver.getAuthenticatedUser();
        log.info("Fetching orders for customer [{}]", user.getEmail());

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        log.info("Found [{}] orders for customer [{}]", orders.size(), user.getEmail());

        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllPendingOrders() {
        log.info("Fetching all pending orders");
        List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.PENDING_PAYMENT);
        log.info("Found [{}] pending orders", orders.size());

        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        User admin = userResolver.getAuthenticatedUser();
        log.info("Confirming order [{}] by admin [{}]", orderId, admin.getEmail());
        orderRepository.updateOrderStatus(orderId, OrderStatus.CONFIRMED, admin.getEmail());
        log.info("Order [{}] successfully confirmed", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        User admin = userResolver.getAuthenticatedUser();
        log.warn("Cancelling order [{}] by admin [{}]", orderId, admin.getEmail());
        orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED, admin.getEmail());
        log.warn("Order [{}] successfully cancelled by admin", orderId);
    }

    @Override
    @Transactional
    public void cancelMyOrder(Long orderId) {
        User user = userResolver.getAuthenticatedUser();
        log.info("Customer [{}] requesting cancellation of order [{}]", user.getEmail(), orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));

        // Ensure the order belongs to this customer
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            log.warn("Customer [{}] attempted to cancel order [{}] that does not belong to them",
                    user.getEmail(), orderId);
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        // Customer can only cancel if payment hasn't been made yet
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Customer [{}] attempted to cancel order [{}] with status [{}]",
                    user.getEmail(), orderId, order.getOrderStatus());
            throw new OrderCancellationNotAllowedException(orderId, order.getOrderStatus());
        }

        orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED, user.getEmail());
        log.info("Order [{}] successfully cancelled by customer [{}]", orderId, user.getEmail());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private OrderResponseDto mapToOrderResponseDTO(Order order) {
        List<OrderItemReponseDto> itemDTOs = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponseDTO)
                .collect(Collectors.toList());

        OrderResponseDto orderResponseDto = new OrderResponseDto(
                order.getOrderId(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getCreatedAt().toString(),
                itemDTOs
        );

        log.debug("Mapped Order [{}] with [{}] items to DTO", order.getOrderId(), itemDTOs.size());
        return orderResponseDto;
    }

    private OrderItemReponseDto mapToOrderItemResponseDTO(OrderItem orderItem) {
        OrderItemReponseDto itemDTO = new OrderItemReponseDto(
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
        log.debug("Mapped OrderItem [{}] x [{}] to DTO", orderItem.getProduct().getName(), orderItem.getQuantity());
        return itemDTO;
    }
}
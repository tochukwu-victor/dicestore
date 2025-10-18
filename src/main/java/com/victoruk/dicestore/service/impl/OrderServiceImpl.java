

package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.constant.ApplicationConstants;
import com.victoruk.dicestore.dto.OrderItemReponseDto;
import com.victoruk.dicestore.dto.OrderResponseDto;
import com.victoruk.dicestore.entity.*;
import com.victoruk.dicestore.repository.CartItemRepository;
import com.victoruk.dicestore.repository.OrderRepository;
import com.victoruk.dicestore.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProfileServiceImpl profileService;

    @Override
    public void createOrder() {
        Customer customer = profileService.getAuthenticatedCustomer();
        log.info("Starting order creation for customer [{}]", customer.getEmail());

        // Fetch cart items
        List<CartItem> cartItems = cartItemRepository.findByCartCustomer(customer);
        if (cartItems.isEmpty()) {
            log.warn("Customer [{}] attempted to create order with empty cart", customer.getEmail());
            throw new RuntimeException("Cart is empty");
        }

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderStatus(ApplicationConstants.ORDER_STATUS_CREATED);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cartItem.getProduct());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getProduct().getPrice()); // or use cartItem.getPrice() if stored
            log.debug("Adding product [{}] x [{}] to order", cartItem.getProduct().getName(), cartItem.getQuantity());
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
                order.getOrderId(), customer.getEmail(), total);

        // Clear cart
        cartItemRepository.deleteAll(cartItems);
        log.info("Cleared cart for customer [{}] after creating order [{}]", customer.getEmail(), order.getOrderId());
    }

    @Override
    public List<OrderResponseDto> getCustomerOrders() {
        Customer customer = profileService.getAuthenticatedCustomer();
        log.info("Fetching orders for customer [{}]", customer.getEmail());

        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        log.info("Found [{}] orders for customer [{}]", orders.size(), customer.getEmail());

        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getAllPendingOrders() {
        log.info("Fetching all pending orders");
        List<Order> orders = orderRepository.findByOrderStatus(ApplicationConstants.ORDER_STATUS_CREATED);
        log.info("Found [{}] pending orders", orders.size());

        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(Long orderId, String orderStatus) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.info("Updating order [{}] to status [{}] by admin [{}]", orderId, orderStatus, email);
        orderRepository.updateOrderStatus(orderId, orderStatus, email);
        log.info("Order [{}] successfully updated to status [{}]", orderId, orderStatus);
    }

    /**
     * Map Order entity to OrderResponseDto
     */
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

    /**
     * Map OrderItem entity to OrderItemResponseDto
     */
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

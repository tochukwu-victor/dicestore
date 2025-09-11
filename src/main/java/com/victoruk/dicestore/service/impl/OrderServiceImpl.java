package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.constant.ApplicationConstants;
import com.victoruk.dicestore.dto.OrderItemReponseDto;
import com.victoruk.dicestore.dto.OrderRequestDto;
import com.victoruk.dicestore.dto.OrderResponseDto;
import com.victoruk.dicestore.entity.*;
import com.victoruk.dicestore.exception.ResourceNotFoundException;
import com.victoruk.dicestore.repository.CartItemRepository;
import com.victoruk.dicestore.repository.OrderRepository;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProfileServiceImpl profileService;

    @Override
    public void createOrder() {
        Customer customer = profileService.getAuthenticatedCustomer();

        // Fetch cart items
        List<CartItem> cartItems = cartItemRepository.findByCartCustomer(customer);
        if (cartItems.isEmpty()) {
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
            return item;
        }).collect(Collectors.toList());

        // Calculate total
        BigDecimal total = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setOrderItems(orderItems);
        order.setTotalPrice(total);

        orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteAll(cartItems);
    }






    @Override
    public List<OrderResponseDto> getCustomerOrders() {

        Customer customer = profileService.getAuthenticatedCustomer();

        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer);

       return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());

    }


    @Override
    public List<OrderResponseDto> getAllPendingOrders() {
        List<Order> orders = orderRepository.findByOrderStatus(ApplicationConstants.ORDER_STATUS_CREATED);
        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(Long orderId, String orderStatus) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        orderRepository.updateOrderStatus(orderId,orderStatus,email);
    }


    /**
     * Map Order entity to OrderResponseDto
     */
    private OrderResponseDto mapToOrderResponseDTO(Order order) {
        // Map Order Items
        List<OrderItemReponseDto> itemDTOs = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponseDTO)
                .collect(Collectors.toList());
        OrderResponseDto orderResponseDto = new OrderResponseDto(order.getOrderId()
                , order.getOrderStatus(), order.getTotalPrice(), order.getCreatedAt().toString()
                , itemDTOs);
        return orderResponseDto;
    }

    /**
     * Map OrderItem entity to OrderItemResponseDto
     */
    private OrderItemReponseDto mapToOrderItemResponseDTO(OrderItem orderItem) {
        OrderItemReponseDto itemDTO = new OrderItemReponseDto(
                orderItem.getProduct().getName(), orderItem.getQuantity(),
                orderItem.getPrice());
        return itemDTO;
    }
}

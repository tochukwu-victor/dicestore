package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.dto.AddToCartRequestDto;
import com.victoruk.dicestore.dto.CartItemDto;
import com.victoruk.dicestore.dto.CartResponseDto;
import com.victoruk.dicestore.entity.*;
import com.victoruk.dicestore.exception.ResourceNotFoundException;
import com.victoruk.dicestore.repository.CartItemRepository;
import com.victoruk.dicestore.repository.CartRepository;
import com.victoruk.dicestore.repository.ProductRepository;
import com.victoruk.dicestore.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProfileServiceImpl profileService;

    @Override
    public void addToCart(AddToCartRequestDto requestDto) {
        Customer customer = profileService.getAuthenticatedCustomer();

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(requestDto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", requestDto.productId().toString()));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(requestDto.quantity());
        item.setPrice(product.getPrice());

        cart.getCartItems().add(item);
        cartRepository.save(cart);
    }

    @Override
    public CartResponseDto getMyCart() {
        Customer customer = profileService.getAuthenticatedCustomer();

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "Customer", customer.getCustomerId().toString()));

        List<CartItemDto> items = cart.getCartItems().stream().map(cartItem ->
                new CartItemDto(
                        cartItem.getProduct().getProductId(),
                        cartItem.getProduct().getName(),
                        cartItem.getPrice(),
                        cartItem.getQuantity()
                )
        ).collect(Collectors.toList());

        return new CartResponseDto(cart.getCartId(), items);
    }

    @Override
    public void removeItemFromCart(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId.toString()));
        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart() {
        Customer customer = profileService.getAuthenticatedCustomer();
        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "Customer", customer.getCustomerId().toString()));
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}

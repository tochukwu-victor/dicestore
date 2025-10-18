
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
import com.victoruk.dicestore.service.IDiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProfileServiceImpl profileService;
    private final IDiscountService discountService;

    @Override
    public void addToCart(AddToCartRequestDto requestDto) {
        Customer customer = profileService.getAuthenticatedCustomer();
        log.info("Adding product [{}] (qty: {}) to cart for customer [{}]",
                requestDto.productId(), requestDto.quantity(), customer.getEmail());

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    log.info("No existing cart found for customer [{}], creating new one", customer.getEmail());
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(requestDto.productId())
                .orElseThrow(() -> {
                    log.error("Product with id [{}] not found for addToCart", requestDto.productId());
                    return new ResourceNotFoundException("Product", "id", requestDto.productId().toString());
                });

        BigDecimal unitFinalPrice = discountService.calculateFinalPrice(
                product.getProductId(),
                product.getPrice()
        );

        log.debug("Final unit price after discount for product [{}]: {}", product.getName(), unitFinalPrice);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(requestDto.quantity());
        item.setPrice(unitFinalPrice);

        cart.getCartItems().add(item);
        cartRepository.save(cart);

        log.info("Product [{}] successfully added to cart [{}]", product.getName(), cart.getCartId());
    }

    @Override
    public CartResponseDto getMyCart() {
        Customer customer = profileService.getAuthenticatedCustomer();
        log.info("Fetching cart for customer [{}]", customer.getEmail());

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> {
                    log.error("Cart not found for customer [{}]", customer.getEmail());
                    return new ResourceNotFoundException("Cart", "Customer", customer.getCustomerId().toString());
                });

        List<CartItemDto> items = cart.getCartItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            BigDecimal originalPrice = product.getPrice();

            BigDecimal discountedPrice = discountService.calculateFinalPrice(
                    product.getProductId(),
                    originalPrice
            );

            log.debug("Cart item [{}] - Original: {}, Discounted: {}, Qty: {}",
                    product.getName(), originalPrice, discountedPrice, cartItem.getQuantity());

            return new CartItemDto(
                    product.getProductId(),
                    product.getName(),
                    originalPrice,
                    discountedPrice,
                    cartItem.getQuantity()
            );
        }).collect(Collectors.toList());

        log.info("Cart [{}] fetched with [{}] items", cart.getCartId(), items.size());
        return new CartResponseDto(cart.getCartId(), items);
    }

    @Override
    public void removeItemFromCart(Long cartItemId) {
        log.info("Removing item [{}] from cart", cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> {
                    log.error("CartItem with id [{}] not found", cartItemId);
                    return new ResourceNotFoundException("CartItem", "id", cartItemId.toString());
                });

        cartItemRepository.delete(item);
        log.info("Item [{}] removed successfully from cart", cartItemId);
    }

    @Override
    public void clearCart() {
        Customer customer = profileService.getAuthenticatedCustomer();
        log.info("Clearing cart for customer [{}]", customer.getEmail());

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> {
                    log.error("Cart not found for customer [{}]", customer.getEmail());
                    return new ResourceNotFoundException("Cart", "Customer", customer.getCustomerId().toString());
                });

        int itemCount = cart.getCartItems().size();
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("Cart [{}] cleared, removed [{}] items", cart.getCartId(), itemCount);
    }
}










//
//package com.victoruk.dicestore.service.impl;
//
//import com.victoruk.dicestore.dto.AddToCartRequestDto;
//import com.victoruk.dicestore.dto.CartItemDto;
//import com.victoruk.dicestore.dto.CartResponseDto;
//import com.victoruk.dicestore.entity.*;
//import com.victoruk.dicestore.exception.ResourceNotFoundException;
//import com.victoruk.dicestore.repository.CartItemRepository;
//import com.victoruk.dicestore.repository.CartRepository;
//import com.victoruk.dicestore.repository.ProductRepository;
//import com.victoruk.dicestore.service.ICartService;
//import com.victoruk.dicestore.service.IDiscountService;   // <-- add
//import com.victoruk.dicestore.util.DiscountUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;                                // <-- add
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class CartServiceImpl implements ICartService {
//
//    private final CartRepository cartRepository;
//    private final CartItemRepository cartItemRepository;
//    private final ProductRepository productRepository;
//    private final ProfileServiceImpl profileService;
//    private final IDiscountService discountService;        // <-- inject
//
//    @Override
//    public void addToCart(AddToCartRequestDto requestDto) {
//        Customer customer = profileService.getAuthenticatedCustomer();
//
//        Cart cart = cartRepository.findByCustomer(customer)
//                .orElseGet(() -> {
//                    Cart newCart = new Cart();
//                    newCart.setCustomer(customer);
//                    return cartRepository.save(newCart);
//                });
//
//        Product product = productRepository.findById(requestDto.productId())
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Product", "id", requestDto.productId().toString())
//                );
//
//        // ✅ compute discounted UNIT price now
//        BigDecimal unitFinalPrice = discountService.calculateFinalPrice(
//                product.getProductId(),
//                product.getPrice()
//        );
//
//        CartItem item = new CartItem();
//        item.setCart(cart);
//        item.setProduct(product);
//        item.setQuantity(requestDto.quantity());
//        // Store discounted unit price in the cart item
//        item.setPrice(unitFinalPrice);
//
//        cart.getCartItems().add(item);
//        cartRepository.save(cart);
//    }
//
//    @Override
//    public CartResponseDto getMyCart() {
//        Customer customer = profileService.getAuthenticatedCustomer();
//
//        Cart cart = cartRepository.findByCustomer(customer)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Cart", "Customer", customer.getCustomerId().toString())
//                );
//
//        List<CartItemDto> items = cart.getCartItems().stream().map(cartItem -> {
//            Product product = cartItem.getProduct();
//
//            // Original price
//            BigDecimal originalPrice = product.getPrice();
//
//            // Use DiscountUtils OR discountService (pick one source of truth)
//            BigDecimal discountedPrice = discountService.calculateFinalPrice(
//                    product.getProductId(),
//                    originalPrice
//            );
//
//            return new CartItemDto(
//                    product.getProductId(),
//                    product.getName(),
//                    originalPrice,
//                    discountedPrice,
//                    cartItem.getQuantity()
//            );
//        }).collect(Collectors.toList());
//
//        return new CartResponseDto(cart.getCartId(), items);
//    }
//
//    @Override
//    public void removeItemFromCart(Long cartItemId) {
//        CartItem item = cartItemRepository.findById(cartItemId)
//                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId.toString()));
//        cartItemRepository.delete(item);
//    }
//
//    @Override
//    public void clearCart() {
//        Customer customer = profileService.getAuthenticatedCustomer();
//        Cart cart = cartRepository.findByCustomer(customer)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Cart", "Customer", customer.getCustomerId().toString())
//                );
//        cart.getCartItems().clear();
//        cartRepository.save(cart);
//    }
//}

package com.victoruk.dicestore.cart.service;

import com.victoruk.dicestore.cart.entity.Cart;
import com.victoruk.dicestore.cart.entity.CartItem;
import com.victoruk.dicestore.cart.dto.AddToCartRequestDto;
import com.victoruk.dicestore.cart.dto.CartItemDto;
import com.victoruk.dicestore.cart.dto.CartResponseDto;
import com.victoruk.dicestore.common.exception.ResourceNotFoundException;
import com.victoruk.dicestore.common.security.AuthenticatedUserResolver;
import com.victoruk.dicestore.product.entity.Product;
import com.victoruk.dicestore.cart.repository.CartItemRepository;
import com.victoruk.dicestore.cart.repository.CartRepository;
import com.victoruk.dicestore.product.repository.ProductRepository;
import com.victoruk.dicestore.discount.service.IDiscountService;
import com.victoruk.dicestore.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final IDiscountService discountService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @Override
    @Transactional
    public void addToCart(AddToCartRequestDto requestDto) {
        User user = authenticatedUserResolver.getAuthenticatedUser();
        log.info("Adding product [{}] (qty: {}) to cart for user [{}]",
                requestDto.productId(), requestDto.quantity(), user.getEmail());

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("No existing cart found for user [{}], creating new one", user.getEmail());
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(requestDto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", requestDto.productId().toString()));

        BigDecimal unitFinalPrice = discountService.calculateFinalPrice(
                product.getProductId(),
                product.getPrice()
        );

        log.debug("Final unit price after discount for product [{}]: {}", product.getName(), unitFinalPrice);

        // Check if product already exists in cart — increment instead of duplicate
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + requestDto.quantity());
            item.setPrice(unitFinalPrice); // refresh price in case discount changed
            log.info("Product [{}] already in cart, updated quantity to [{}]",
                    product.getName(), item.getQuantity());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(requestDto.quantity());
            item.setPrice(unitFinalPrice);
            cart.getCartItems().add(item);
            log.info("Product [{}] successfully added to cart [{}]", product.getName(), cart.getCartId());
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartResponseDto getMyCart() {
        User user = authenticatedUserResolver.getAuthenticatedUser();
        log.info("Fetching cart for user [{}]", user.getEmail());

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user", user.getUserId().toString()));

        List<CartItemDto> items = cart.getCartItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            BigDecimal originalPrice = product.getPrice();

            BigDecimal discountedPrice = discountService.calculateFinalPrice(
                    product.getProductId(),
                    originalPrice
            );

            BigDecimal lineTotal = discountedPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            log.debug("Cart item [{}] - Original: {}, Discounted: {}, Qty: {}, LineTotal: {}",
                    product.getName(), originalPrice, discountedPrice, cartItem.getQuantity(), lineTotal);

            return new CartItemDto(
                    product.getProductId(),
                    product.getName(),
                    originalPrice,
                    discountedPrice,
                    cartItem.getQuantity(),
                    lineTotal
            );
        }).collect(Collectors.toList());

        BigDecimal grandTotal = items.stream()
                .map(CartItemDto::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Cart [{}] fetched with [{}] items, grandTotal: {}", cart.getCartId(), items.size(), grandTotal);

        return new CartResponseDto(cart.getCartId(), items, items.size(), grandTotal);
    }

    @Override
    @Transactional
    public void updateQuantity(Long cartItemId, int quantityChange) {
        log.info("Updating quantity for cart item [{}] by delta [{}]", cartItemId, quantityChange);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId.toString()));

        int newQuantity = item.getQuantity() + quantityChange;

        if (newQuantity <= 0) {
            // quantity hits 0 or below — remove the item entirely
            cartItemRepository.delete(item);
            log.info("Cart item [{}] removed as quantity reached zero", cartItemId);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.info("Cart item [{}] quantity updated to [{}]", cartItemId, newQuantity);
        }
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long cartItemId) {
        log.info("Removing item [{}] from cart", cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId.toString()));

        cartItemRepository.delete(item);
        log.info("Item [{}] removed successfully from cart", cartItemId);
    }

    @Override
    @Transactional
    public void clearCart() {
        User user = authenticatedUserResolver.getAuthenticatedUser();
        log.info("Clearing cart for user [{}]", user.getEmail());

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user", user.getUserId().toString()));

        int itemCount = cart.getCartItems().size();
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("Cart [{}] cleared, removed [{}] items", cart.getCartId(), itemCount);
    }

    @Override
    @Transactional
    public int getCartItemCount() {
        User user = authenticatedUserResolver.getAuthenticatedUser();
        log.info("Fetching cart item count for user [{}]", user.getEmail());

        return cartRepository.findByUser(user)
                .map(cart -> cart.getCartItems().size())
                .orElse(0);
    }
}
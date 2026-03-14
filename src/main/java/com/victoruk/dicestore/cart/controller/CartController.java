package com.victoruk.dicestore.cart.controller;

import com.victoruk.dicestore.cart.dto.AddToCartRequestDto;
import com.victoruk.dicestore.cart.dto.CartResponseDto;
import com.victoruk.dicestore.cart.service.ICartService;
import com.victoruk.dicestore.common.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management for authenticated users")
public class CartController {

    private final ICartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Add item to cart",
            description = "Adds a product to the authenticated user's cart. Creates a cart if one does not exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> addToCart(@RequestBody AddToCartRequestDto requestDto) {
        log.info("Request to add product {} with quantity {} to cart", requestDto.productId(), requestDto.quantity());
        cartService.addToCart(requestDto);
        log.info("Product {} added successfully to cart", requestDto.productId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get current user's cart",
            description = "Returns all cart items with original price, discounted price, quantity, and total item count.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<CartResponseDto> getCart() {
        log.info("Fetching current user's cart");
        CartResponseDto response = cartService.getMyCart();
        log.info("Cart fetched successfully with {} items", response.items().size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{cartItemId}")
    @Operation(summary = "Remove item from cart",
            description = "Removes a specific item from the cart by its cart item ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        log.info("Request to remove cart item with ID {}", cartItemId);
        cartService.removeItemFromCart(cartItemId);
        log.info("Cart item {} removed successfully", cartItemId);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "Update cart item quantity",
            description = "Increments or decrements the quantity of a specific cart item. " +
                    "Pass a positive value to increase (e.g. +1) or a negative value to decrease (e.g. -1). " +
                    "If the resulting quantity drops to zero or below, the item is automatically removed from the cart."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quantity updated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart item not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PatchMapping("/items/{cartItemId}/quantity")
    public ResponseEntity<Void> updateCartItemQuantity(
            @Parameter(description = "ID of the cart item to update", required = true)
            @PathVariable Long cartItemId,

            @Parameter(description = "Amount to change the quantity by. Use +1 to increment, -1 to decrement.", required = true)
            @RequestParam int quantityChange) {

        cartService.updateQuantity(cartItemId, quantityChange);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart",
            description = "Removes all items from the authenticated user's cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<Void> clearCart() {
        log.info("Request to clear entire cart");
        cartService.clearCart();
        log.info("Cart cleared successfully");
        return ResponseEntity.ok().build();
    }
}
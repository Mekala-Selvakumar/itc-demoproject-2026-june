package com.learn.retailordersystem.controller;

 import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learn.retailordersystem.dto.CartRequest;
import com.learn.retailordersystem.dto.CartResponse;
import com.learn.retailordersystem.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public CartResponse addToCart(@Valid @RequestBody CartRequest request) {
        return cartService.addToCart(request);
    }

    @GetMapping("/{customerId}")
    public List<CartResponse> getCart(@PathVariable Long customerId) {
        return cartService.getCartByCustomer(customerId);
    }

    @DeleteMapping("/{cartId}")
    public String removeFromCart(@PathVariable Long cartId) {
        cartService.removeFromCart(cartId);
        return "Item removed from cart";
    }
}
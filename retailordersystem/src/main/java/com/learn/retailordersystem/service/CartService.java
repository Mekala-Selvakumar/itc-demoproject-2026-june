package com.learn.retailordersystem.service;

 import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.learn.retailordersystem.dto.CartRequest;
import com.learn.retailordersystem.dto.CartResponse;
 import com.learn.retailordersystem.entity.Cart;
import com.learn.retailordersystem.entity.Customer;
 import com.learn.retailordersystem.entity.Product;
import com.learn.retailordersystem.exceptions.ResourceNotFoundException;
import com.learn.retailordersystem.repository.CartRepository;
import com.learn.retailordersystem.repository.CustomerRepository;
 import com.learn.retailordersystem.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartResponse addToCart(CartRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Cart cart = Cart.builder()
                .customer(customer)
                .product(product)
                .quantity(request.getQuantity())
                .build();

        Cart saved = cartRepository.save(cart);

        return map(saved);
    }

    public List<CartResponse> getCartByCustomer(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return cartRepository.findByCustomer(customer)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public void removeFromCart(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartRepository.delete(cart);
    }

    private CartResponse map(Cart cart) {

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomer().getCustomerId())
                .productId(cart.getProduct().getProductId())
                .productName(cart.getProduct().getProductName())
                .price(cart.getProduct().getPrice())
                .quantity(cart.getQuantity())
                .totalPrice(cart.getProduct().getPrice() * cart.getQuantity())
                .build();
    }
}

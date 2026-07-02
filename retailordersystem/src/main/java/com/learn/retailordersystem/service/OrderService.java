package com.learn.retailordersystem.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.retailordersystem.dto.OrderItemRequest;
import com.learn.retailordersystem.dto.OrderItemResponse;
import com.learn.retailordersystem.dto.OrderRequest;
import com.learn.retailordersystem.dto.OrderResponse;
import com.learn.retailordersystem.entity.Customer;
import com.learn.retailordersystem.entity.Order;
import com.learn.retailordersystem.entity.OrderItem;
import com.learn.retailordersystem.entity.Product;
import com.learn.retailordersystem.exceptions.ResourceNotFoundException;
import com.learn.retailordersystem.repository.CartRepository;
import com.learn.retailordersystem.repository.CustomerRepository;
import com.learn.retailordersystem.repository.OrderRepository;
import com.learn.retailordersystem.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setPaymentStatus("SUCCESS");

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        for (OrderItemRequest itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .order(order)
                    .build();

            total += item.getSubTotal();
            items.add(item);
        }

        order.setOrderItems(items);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        cartRepository.deleteByCustomer(customer);

        return map(saved);
    }

    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return map(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return orderRepository.findByCustomer(customer)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        orderRepository.delete(order);
    }

    private OrderResponse map(Order order) {

        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(i -> OrderItemResponse.builder()
                        .productId(i.getProduct().getProductId())
                        .productName(i.getProduct().getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subTotal(i.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderDate(order.getOrderDate())
                .items(items)
                .build();
    }
}
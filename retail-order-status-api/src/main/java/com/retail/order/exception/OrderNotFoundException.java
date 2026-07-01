package com.retail.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderCode) {
        super("Order not found: " + orderCode);
    }
}

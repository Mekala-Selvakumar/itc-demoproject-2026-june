 package com.learn.service;
 
 import java.util.List;

public class OrderService {

    public double calculateTotal(List<Double> items, String customerType) {

        double total = 0;

        for (Double item : items) {
            total += item;
        }

        double discount = 0;

        if (customerType.equals("PREMIUM")) {
            discount = total * 0.15;
        }

        if (total > 1000) {
            discount = total * 0.10;
        }

        double shippingCharge = 50;

        if (total > 500) {
            shippingCharge = 0;
        }

        return total - discount + shippingCharge;
    }
}

package com.learn.retailordersystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learn.retailordersystem.entity.Customer;
import com.learn.retailordersystem.entity.Order;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer(Customer customer);

    List<Order> findByOrderDate(LocalDate orderDate);

    List<Order> findByPaymentStatus(String paymentStatus);
}
package com.learn.retailordersystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learn.retailordersystem.entity.Cart;
import com.learn.retailordersystem.entity.Customer;
import com.learn.retailordersystem.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByCustomer(Customer customer);

    Optional<Cart> findByCustomerAndProduct(Customer customer, Product product);

    void deleteByCustomer(Customer customer);
}
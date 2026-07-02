package com.learn.retailordersystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learn.retailordersystem.entity.Product;


import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
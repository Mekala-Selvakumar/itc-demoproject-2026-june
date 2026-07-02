package com.learn.retailordersystem.service;

 import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.learn.retailordersystem.dto.CustomerRequest;
import com.learn.retailordersystem.dto.CustomerResponse;
import com.learn.retailordersystem.entity.Customer;
import com.learn.retailordersystem.exceptions.ResourceNotFoundException;
import com.learn.retailordersystem.repository.CustomerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {

        Customer customer = Customer.builder()
                .customerName(request.getCustomerName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .city(request.getCity())
                .build();

        Customer saved = customerRepository.save(customer);

        return mapToResponse(saved);
    }

    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapToResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setCustomerName(request.getCustomerName());
        customer.setEmail(request.getEmail());
        customer.setMobile(request.getMobile());
        customer.setCity(request.getCity());

        return mapToResponse(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customerRepository.delete(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .email(customer.getEmail())
                .mobile(customer.getMobile())
                .city(customer.getCity())
                .build();
    }
}
package com.example.application.data.service.customers;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.data.entity.customers.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
package com.example.application.data.service.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

}

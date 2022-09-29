package com.example.application.data.service.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.data.entity.products.Category;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

}

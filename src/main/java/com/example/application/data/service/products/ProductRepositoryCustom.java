package com.example.application.data.service.products;

import java.util.List;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;

public interface ProductRepositoryCustom {
	
	List<Product> filterByCategory(Category category);

}

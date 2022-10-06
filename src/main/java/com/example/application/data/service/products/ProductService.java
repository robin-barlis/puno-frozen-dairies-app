package com.example.application.data.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.Size;


@Service
public class ProductService {
	
	private final ProductRepository repository;

	@Autowired
	public ProductService(ProductRepository repository) {
		this.repository = repository;
	}

	
	public Optional<Product> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Product> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Product> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Product update(Object entity) {
		return repository.save((Product)entity);
		
	}


}

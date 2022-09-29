package com.example.application.data.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.Category;


@Service
public class CategoryService {
	
	private final CategoryRepository repository;

	@Autowired
	public CategoryService(CategoryRepository repository) {
		this.repository = repository;
	}

	
	public Optional<Category> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Category> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Category> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Category update(Object entity) {
		return repository.save((Category)entity);
		
	}


}

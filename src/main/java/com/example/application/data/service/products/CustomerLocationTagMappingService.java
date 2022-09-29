package com.example.application.data.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.CustomerTag;


@Service
public class CustomerLocationTagMappingService {
	
	private final CustomerTagRepository repository;

	@Autowired
	public CustomerLocationTagMappingService(CustomerTagRepository repository) {
		this.repository = repository;
	}

	public Optional<CustomerTag> get(Integer id) {
		return repository.findById(id);
	}

	public CustomerTag update(CustomerTag entity) {
		return repository.save(entity);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<CustomerTag> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<CustomerTag> listAll(Sort sort) {
		return repository.findAll(sort);
	}

}

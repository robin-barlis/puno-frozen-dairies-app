package com.example.application.data.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.Size;


@Service
public class SizesService {
	
	private final SizesRepository repository;

	@Autowired
	public SizesService(SizesRepository repository) {
		this.repository = repository;
	}

	
	public Optional<Size> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Size> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Size> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Size update(Object entity) {
		return repository.save((Size)entity);
		
	}


}

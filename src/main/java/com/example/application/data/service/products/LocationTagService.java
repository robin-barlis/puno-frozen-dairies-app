package com.example.application.data.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.LocationTag;


@Service
public class LocationTagService {
	
	private final LocationTagRepository repository;

	@Autowired
	public LocationTagService(LocationTagRepository repository) {
		this.repository = repository;
	}

	
	public Optional<LocationTag> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<LocationTag> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<LocationTag> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public LocationTag update(Object entity) {
		return repository.save((LocationTag)entity);
		
	}


}

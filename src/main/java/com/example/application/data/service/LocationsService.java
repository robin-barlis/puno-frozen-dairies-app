package com.example.application.data.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.PfdiLocation;

@Service
public class LocationsService {

	private final LocationsRepository repository;

	@Autowired
	public LocationsService(LocationsRepository repository) {
		this.repository = repository;
	}

	public Optional<PfdiLocation> get(Integer id) {
		return repository.findById(id);
	}

	public PfdiLocation update(PfdiLocation entity) {
		return repository.save(entity);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<PfdiLocation> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<PfdiLocation> listAll(Sort sort) {
		return repository.findAll(sort);
	}

}

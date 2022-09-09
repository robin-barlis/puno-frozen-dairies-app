package com.example.application.data.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.PfdiPosition;

@Service
public class PositionsService {

	private final PositionsRepository repository;

	@Autowired
	public PositionsService(PositionsRepository repository) {
		this.repository = repository;
	}

	public Optional<PfdiPosition> get(Integer id) {
		return repository.findById(id);
	}

	public PfdiPosition update(PfdiPosition entity) {
		return repository.save(entity);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<PfdiPosition> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<PfdiPosition> listAll(Sort sort) {
		return repository.findAll(sort);
	}

}

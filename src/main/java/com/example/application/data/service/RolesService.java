package com.example.application.data.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.PfdiRoles;

@Service
public class RolesService {

	private final RolesRepository repository;

	@Autowired
	public RolesService(RolesRepository repository) {
		this.repository = repository;
	}

	public Optional<PfdiRoles> get(Integer id) {
		return repository.findById(id);
	}

	public PfdiRoles update(PfdiRoles entity) {
		return repository.save(entity);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<PfdiRoles> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<PfdiRoles> listAll(Sort sort) {
		return repository.findAll(sort);
	}

}

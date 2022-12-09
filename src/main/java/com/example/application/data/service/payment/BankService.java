package com.example.application.data.service.payment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.payment.Banks;

@Service
public class BankService {

	private final BankRepository repository;

	@Autowired
	public BankService(BankRepository repository) {
		this.repository = repository;
	}

	public Optional<Banks> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Banks> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Banks> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Banks update(Object entity) {
		
		return repository.save((Banks) entity);

	}

	public List<Banks> updateAll(Collection<Banks> entities) {
		return repository.saveAll(entities);

	}


}

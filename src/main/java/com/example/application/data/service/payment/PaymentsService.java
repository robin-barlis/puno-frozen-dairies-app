package com.example.application.data.service.payment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.payment.Payment;

@Service
public class PaymentsService {

	private final PaymentsRepository repository;

	@Autowired
	public PaymentsService(PaymentsRepository repository) {
		this.repository = repository;
	}

	public Optional<Payment> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Payment> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Payment> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Payment update(Object entity) {
		
		return repository.save((Payment) entity);

	}

	public List<Payment> updateAll(Collection<Payment> entities) {
		return repository.saveAll(entities);

	}


}

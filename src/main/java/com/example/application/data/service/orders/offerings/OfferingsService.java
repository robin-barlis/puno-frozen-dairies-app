package com.example.application.data.service.orders.offerings;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.orders.offerings.Offerings;

@Service
public class OfferingsService implements Serializable{

	private static final long serialVersionUID = 1L;
	private final OfferingsRepository repository;

	@Autowired
	public OfferingsService(OfferingsRepository repository) {
		this.repository = repository;
	}

	public Optional<Offerings> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Offerings> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Offerings> listAll(Sort sort) {
		return repository.findAll(sort);
	}
	


	public Offerings update(Offerings entity) {
			return repository.save((Offerings) entity);

	}

	public List<Offerings> updateAll(Collection<Offerings> entities) {
		return repository.saveAll(entities);

	}
	


}

package com.example.application.data.service.orders;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.stock.ItemStock;


@Service
public class OrdersService {
	
	private final OrdersRepository repository;

	@Autowired
	public OrdersService(OrdersRepository repository) {
		this.repository = repository;
	}

	
	public Optional<Order> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<Order> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Order> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Order update(Object entity) {
		return repository.save((Order)entity);
		
	}
	
	public List<Order> updateAll(Collection<Order> entities) {
		return repository.saveAll(entities);
		
	}


}

package com.example.application.data.service.stock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;


@Service
public class ItemStockService {
	
	private final ItemStockRepository repository;

	@Autowired
	public ItemStockService(ItemStockRepository repository) {
		this.repository = repository;
	}

	
	public Optional<ItemStock> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<ItemStock> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<ItemStock> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public ItemStock update(Object entity) {
		return repository.save((ItemStock)entity);
		
	}
	
	public List<ItemStock> updateAll(Collection<ItemStock> entities) {
		return repository.saveAll(entities);
		
	}


}

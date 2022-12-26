package com.example.application.data.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;


@Service
public class ProductPriceService {
	
	private final ProductPriceRepository repository;

	@Autowired
	public ProductPriceService(ProductPriceRepository repository) {
		this.repository = repository;
	}

	
	public Optional<ProductPrice> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<ProductPrice> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<ProductPrice> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public ProductPrice update(Object entity) {
		return repository.save((ProductPrice)entity);
		
	}
	
	public void deleteByProduct(Product product) {
		repository.deleteByProduct(product);
	}


}

package com.example.application.data.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.AbstractEntity;


public interface PfdiService {
	
	public Optional<?> get(Integer id);

	public AbstractEntity update(Object entity);

	public void delete(Integer id);

	public Page<?> list(Pageable pageable);

	public List<?> listAll(Sort sort);
}

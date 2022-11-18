package com.example.application.data.service.orders;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.orders.DocumentTrackingNumber;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.stock.ItemStock;

@Service
public class DocumentTrackingNumberService {

	private final DocumentTrackingNumberRepository repository;

	@Autowired
	public DocumentTrackingNumberService(DocumentTrackingNumberRepository repository) {
		this.repository = repository;
	}

	public Optional<DocumentTrackingNumber> get(Integer id) {
		return repository.findById(id);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}

	public Page<DocumentTrackingNumber> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<DocumentTrackingNumber> listAll(Sort sort) {
		return repository.findAll(sort);
	}

	public DocumentTrackingNumber update(Object entity) {
		return repository.save((DocumentTrackingNumber) entity);

	}

	public List<DocumentTrackingNumber> updateAll(Collection<DocumentTrackingNumber> entities) {
		return repository.saveAll(entities);

	}

	public Integer getInvoiceNumber() {
		Integer docNumber = repository.findInvoiceNumber();	
		return docNumber == null ? 0 : docNumber;
	}
	
	public Integer getDeliveryReceiptNumber() {
		Integer docNumber = repository.findDeliveryReceiptNumber();	
		return docNumber == null ? 0 : docNumber;
	}
	
	public Integer getStockTransferNumber() {
		Integer docNumber = repository.findSTNumber();	
		return docNumber == null ? 0 : docNumber;
	}
	
	public Integer getPaymentNumber() {
		Integer docNumber = repository.findPaymentNumber();	
		return docNumber == null ? 0 : docNumber;
	}
	
	public DocumentTrackingNumber findByType(String type) {
		return repository.findByType(type);
	}

}

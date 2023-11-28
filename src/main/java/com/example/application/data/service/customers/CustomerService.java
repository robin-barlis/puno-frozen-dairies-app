package com.example.application.data.service.customers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.customers.Customer;

@Service
public class CustomerService {

	@Autowired
    private final CustomerRepository repository;

    @Autowired
    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Optional<Customer> get(Integer id) {
        return repository.findById(id);
    }

    public Customer update(Customer customer) {
    	if (customer.getId() == null) {
            
            return repository.save(customer);
        } else {
        	return repository.save(customer);
        }
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Customer> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    public List<Customer> listAll(Sort sort) {
        return repository.findAll(sort);
    }
    
    public Map<String, List<Customer>> listAllByCustomerTag() {
    	List<Customer> allCustomer = repository.findAll(Sort.by(Sort.Order.asc("storeName")));
    	Map<String, List<Customer>> customerByCategory = allCustomer.stream().collect(Collectors.groupingBy(customer -> (String) customer.getCustomerTagId().getCustomerTagName()));
    	
    	
        return customerByCategory;
    }

    public int count() {
        return (int) repository.count();
    }

	public Customer changeUserStatus(Customer currentCustomer, boolean newStatus) {
		Customer currentCustomerToUpdate = repository.findById(currentCustomer.getId()).orElseGet(null);
		return repository.save(currentCustomerToUpdate);
	}



}

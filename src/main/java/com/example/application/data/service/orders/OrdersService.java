package com.example.application.data.service.orders;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItemSummary;
import com.example.application.data.entity.orders.OrderItems;
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
	
	public List<Order> findReadyForPaymentOrdersByCustomerName(String customerName) {
		
		List<Order> ordersWithInvoices =repository.findReadyForPaymentOrdersByCustomerName(customerName).
				stream().filter(order-> { 
						return !BigDecimal.ZERO.equals(order.getBalance()) 
								&& (order.getInvoiceId() != null || order.getStockTransferId() != null);
		}).collect(Collectors.toList());
			
		// filter orders with zero balance
		return ordersWithInvoices;
	}
	
	public List<Order> findOrdersForPayment() {
		return repository.findReadyForPaymentOrders();	
	}


	public Order update(Object entity) {
		Order order = repository.save((Order) entity);
		
		List<ItemStock> itemInv = order.getOrderItems().stream().map(e -> e.getItemInventory()).collect(Collectors.toList());

		return repository.save((Order) entity);

	}

	public List<Order> updateAll(Collection<Order> entities) {
		return repository.saveAll(entities);

	}

	public Integer getLastId() {
		Integer lastStockOrderNumnber = repository.findMaxSONumber();	
		return lastStockOrderNumnber == null ? 0 : lastStockOrderNumnber;
	}
	
	public Set<OrderItems> getOrderItemSummary(Integer orderId) {
		Order order = repository.findById(orderId).orElse(null);
		
		if (order != null) {
			
			Set<OrderItems> orderItems = order.getOrderItems();
			return orderItems;
		}
		
		return null;
		
	}

}

package com.example.application.data.entity.orders;

import java.time.LocalDateTime;

import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.products.Size;

public class OrderItemSummary {
	
	Integer stockOrderNumber;
	LocalDateTime orderDate;
	
	Customer customer;
	
	String flavor;
	Integer sizeQuantity;
	Size size;
	Order order;
	public Integer getStockOrderNumber() {
		return stockOrderNumber;
	}
	public void setStockOrderNumber(Integer stockOrderNumber) {
		this.stockOrderNumber = stockOrderNumber;
	}
	public LocalDateTime getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public String getFlavor() {
		return flavor;
	}
	public void setFlavor(String flavor) {
		this.flavor = flavor;
	}
	public Integer getSizeQuantity() {
		return sizeQuantity;
	}
	public void setSizeQuantity(Integer sizeQuantity) {
		this.sizeQuantity = sizeQuantity;
	}
	public Size getSize() {
		return size;
	}
	public void setSize(Size size) {
		this.size = size;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public OrderItemSummary(Integer stockOrderNumber, LocalDateTime orderDate, Customer customer, String flavor,
			Integer sizeQuantity, Size size, Order order) {
		super();
		this.stockOrderNumber = stockOrderNumber;
		this.orderDate = orderDate;
		this.customer = customer;
		this.flavor = flavor;
		this.sizeQuantity = sizeQuantity;
		this.size = size;
		this.order = order;
	}
		
	
}

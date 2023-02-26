package com.example.application.data;

public enum OrderStatus {
    DRAFT("Draft"),  
    FOR_DELIVERY("For Delivery"),
    DELIVERED("Delivered"),
    REOPENED("Reopened"),
	CANCELLED("Cancelled");
	
	private String orderStatusName;

	public String getOrderStatusName() {
		return orderStatusName;
	}
	
	private OrderStatus(String orderStatusName) {
		this.orderStatusName = orderStatusName;
	}
	
	
}

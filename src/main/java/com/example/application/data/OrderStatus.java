package com.example.application.data;

public enum OrderStatus {
    NEW_ORDER("New Order"), 
    FOR_CHECKING("For Checking"), 
    FOR_EDITING("For Editing"), 
    CHECKED("Checked"), 
    IN_TRANSIT("In Transit"), 
    DELIVERED("Delivered"), 
    FOR_PAYMENT("Waiting for payment"), 
    PAID("Paid"), 
    COMPLETED("Completed");
	
	private String orderStatusName;

	public String getOrderStatusName() {
		return orderStatusName;
	}
	
	private OrderStatus(String orderStatusName) {
		this.orderStatusName = orderStatusName;
	}
	
	
}

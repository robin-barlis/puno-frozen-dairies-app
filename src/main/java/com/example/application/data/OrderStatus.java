package com.example.application.data;

public enum OrderStatus {
    DRAFT("Draft"), 
    FOR_CHECKING("For Checking"), 
    FOR_EDITING("For Editing"), 
    CHECKED("Checked"),
    FOR_DELIVERY("For Delivery"),
    DELIVERED("Delivered"), 
    PARTIALLY_PAID("Partially Paid"), 
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

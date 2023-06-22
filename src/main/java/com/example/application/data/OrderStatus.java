package com.example.application.data;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

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
	public Span getBadge() {
		if (this.equals(DRAFT)) {
			Span badge = new Span(createIcon(VaadinIcon.CLOCK),
	                new Span(orderStatusName));
			badge.getElement().getThemeList().add("badge");
			return badge;
		} else if (this.equals(FOR_DELIVERY)) {
			Span badge = new Span(createIcon(VaadinIcon.TRUCK),
	                new Span(orderStatusName));
			badge.getElement().getThemeList().add("badge contrast");
			return badge;
		} else if (this.equals(DELIVERED)) {
			Span badge = new Span(createIcon(VaadinIcon.CHECK),
	                new Span(orderStatusName));
			badge.getElement().getThemeList().add("badge success");
			return badge;
		} else if (this.equals(CANCELLED)) {
			Span badge = new Span(createIcon(VaadinIcon.BAN),
	                new Span(orderStatusName));
			badge.getElement().getThemeList().add("badge error");
			return badge;
		}
		return null;
		
	}
	
	private OrderStatus(String orderStatusName) {
		this.orderStatusName = orderStatusName;
	}
	
	 private Icon createIcon(VaadinIcon vaadinIcon) {
	        Icon icon = vaadinIcon.create();
	        icon.getStyle().set("padding", "var(--lumo-space-xs");
	        return icon;
	    }
	
	
}

package com.example.application.data;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public enum OrderStatus {
	DRAFT("Draft", "Draft"), 
	FOR_DELIVERY("For Delivery", "For Delivery"), 
	DELIVERED("Delivered", "Delivered"), 
	REOPENED("Reopened", "Reopened"), 
	SUBMITTED("Submitted", "New Order"),
	CANCELLED("Cancelled", "Cancelled Order");

	private String orderStatusName;
	private String orderLabel;

	public String getOrderStatusName() {
		return orderStatusName;
	}

	public Span getBadge() {
		if (this.equals(DRAFT)) {
			Span badge = new Span(createIcon(VaadinIcon.CLOCK), new Span(orderLabel));
			badge.getElement().getThemeList().add("badge contrast");
			return badge;
		} else if (this.equals(FOR_DELIVERY)) {
			Span badge = new Span(createIcon(VaadinIcon.TRUCK), new Span(orderLabel));
			badge.getElement().getThemeList().add("badge success");
			return badge;
		} else if (this.equals(DELIVERED)) {
			Span badge = new Span(createIcon(VaadinIcon.CHECK), new Span(orderLabel));
			badge.getElement().getThemeList().add("badge success");
			return badge;
		} else if (this.equals(CANCELLED)) {
			Span badge = new Span(createIcon(VaadinIcon.BAN), new Span(orderLabel));
			badge.getElement().getThemeList().add("badge error");
			return badge;
		} else if (this.equals(SUBMITTED)) {
			Span badge = new Span(createIcon(VaadinIcon.PLUS_SQUARE_O), new Span(orderLabel));
			badge.getElement().getThemeList().add("badge success");
			return badge;
		} else {
			Span badge = new Span(new Span(orderLabel));
			badge.getElement().getThemeList().add("badge success");
			return badge;
			
		}

	}

	private OrderStatus(String orderStatusName, String orderLabel) {
		this.orderStatusName = orderStatusName;
		this.orderLabel = orderLabel;
	}

	private Icon createIcon(VaadinIcon vaadinIcon) {
		Icon icon = vaadinIcon.create();
		icon.getStyle().set("padding", "var(--lumo-space-xs");
		return icon;
	}

}

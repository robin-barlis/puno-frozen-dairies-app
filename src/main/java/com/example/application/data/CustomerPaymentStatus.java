package com.example.application.data;

import java.math.BigDecimal;

import com.example.application.utils.PfdiUtil;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public enum CustomerPaymentStatus {
    FULL_PAYMENT("Full Payment"),
    PARTIAL_PAYMENT("Partial Payment");
	
	private String paymentStatusName;

	public String getOrderStatusName() {
		return paymentStatusName;
	}
	public Span getBadge(BigDecimal amount) {
		
		Span badge = new Span(createIcon(this.equals(PARTIAL_PAYMENT) ? VaadinIcon.CLOCK : VaadinIcon.CHECK), new Span(PfdiUtil.getCurrencyAmount(amount)));
		if (this.equals(PARTIAL_PAYMENT)) {
			badge.getElement().getThemeList().add("badge contrast");
			return badge;
		} else if (this.equals(FULL_PAYMENT)) {
			badge.getElement().getThemeList().add("badge success");
			return badge;
		} 
		return null;
		
	}
	
	private CustomerPaymentStatus(String orderStatusName) {
		this.paymentStatusName = orderStatusName;
	}
	
	 private Icon createIcon(VaadinIcon vaadinIcon) {
	        Icon icon = vaadinIcon.create();
	        icon.getStyle().set("padding", "var(--lumo-space-xs");
	        return icon;
	    }
	
	
}

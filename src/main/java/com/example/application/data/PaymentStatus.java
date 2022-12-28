package com.example.application.data;

public enum PaymentStatus {

	PAID("Paid", "badge primary pill"), 
	PARTIALLY_PAID("Partially Paid", "badge success pill"), 
	FOR_VERIFICATION("For Verification", "badge success pill"),
	PARTIAL_FOR_VERIFICATION("Partial, For Verification", "badge success contrast pill"),
	UNPAID("Unpaid", "badge error pill");

	private String paymentStatusName;
	private String badge;

	public String getPaymentStatusName() {
		return paymentStatusName;
	}

	private PaymentStatus(String paymentStatusName, String badge) {
		this.paymentStatusName = paymentStatusName;
		this.badge=badge;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

}

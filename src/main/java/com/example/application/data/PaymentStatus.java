package com.example.application.data;

public enum PaymentStatus {

	PAID("Paid"), 
	PARTIALLY_PAID("Partially Paid"), 
	FOR_VERIFICATION("For Verification");

	private String paymentStatusName;

	public String getPaymentStatusName() {
		return paymentStatusName;
	}

	private PaymentStatus(String paymentStatusName) {
		this.paymentStatusName = paymentStatusName;
	}

}

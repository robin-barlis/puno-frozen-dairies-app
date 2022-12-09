package com.example.application.data;

public enum PaymentMode {
	
	CASH("Cash"),
	CHEQUE("Cheque"),
	ONLINE_REMITTANCE("Online Remittance");
	
	public String getName() {
		return name;
	}
	
	private String name;
	
	private PaymentMode(String name) {
		this.name = name;
	}

}

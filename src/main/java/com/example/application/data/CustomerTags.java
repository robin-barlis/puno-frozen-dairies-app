package com.example.application.data;

public enum CustomerTags {
	COMPANY_OWNED("Company Owned"), 
	RELATIVE_OWNED("Relative Owned"), 
	DEALER("Dealer"),
	RESELLER("Reseller"),
	PARTNER("Partner"),
	MAIN_STORE("Main Store");
	
	private String customerTagName;

	CustomerTags(String customerTagName) {
		this.customerTagName = customerTagName;
	}
	
	public String getCustomerTagName() {
		return customerTagName;
	}
}

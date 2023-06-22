package com.example.application.data;

public enum DiscountType {
	
	FixedAmount("Fixed Amount"),
	Percentage("Percentage"),
	BuyGetDiscount("Buy/Get"),
	FreeItems("Free Products");	
	
	private String discountLabel;

	DiscountType(String discountLabel) {
		this.discountLabel = discountLabel;
	}
	
	public String getDiscountLabel() {
		return discountLabel;
	}

}

package com.example.application.views.order.offerings;

import com.example.application.data.DiscountType;

public class FixedAmountPercentageDiscount extends AbstractOffering {
	
	private Double value;

	public FixedAmountPercentageDiscount(DiscountType discountType, Double value) {
		super(discountType);
		this.setValue(value);
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
	
	

}

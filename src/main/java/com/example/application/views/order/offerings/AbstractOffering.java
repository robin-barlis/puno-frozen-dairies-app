package com.example.application.views.order.offerings;

import com.example.application.data.DiscountType;

public class AbstractOffering {
	protected DiscountType discountType;
	
	public AbstractOffering(DiscountType discountType) {
		this.discountType = discountType;
	}
	
	public DiscountType getDiscountType() {
		return discountType;
	};
	

}

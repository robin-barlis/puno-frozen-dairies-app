package com.example.application.data;

public enum Promotions {
	
	
	DISCOUNT("Discount"),
	BUY_1_TAKE_1("But 1 Take 1"),
	FREE_ITEMS("Free Items"),
	BUY_1_TAKE_OTHER("Buy 1 Take Other Product");
	
	private String promotionLabel;

	Promotions(String promotionLabel) {
		this.promotionLabel = promotionLabel;
	}

	public String getPromotionLabel() {
		return promotionLabel;
	}

}

package com.example.application.utils;

import java.text.NumberFormat;

public class PfdiUtil {
	
	
	public static final NumberFormat getFormatter() {
		
		NumberFormat currencyInstanceFormat = NumberFormat.getCurrencyInstance();
		currencyInstanceFormat.setMinimumFractionDigits(2);
		currencyInstanceFormat.setMaximumFractionDigits(2);
		
		
		return currencyInstanceFormat;
	}

}

package com.example.application.data;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import net.bytebuddy.asm.Advice.This;

public enum SoaPeriodType {
	
	CURRENT_MONTH("Current Month"),
//	SPECIFIC_MONTH("Specific Year & Month"),
	DATE_RANGE("Date Range");
	
	private String label;

	SoaPeriodType(String label) {
	
		this.label = label;
	}

	public String getLabel() {
		if (this == SoaPeriodType.CURRENT_MONTH) {
			LocalDate currentDate = LocalDate.now();
			String currentYearMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
					+ " " + currentDate.getYear();
			return label + " - " + currentYearMonth;
		}
		return label;
	}
	

}

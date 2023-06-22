package com.example.application.reports;

import java.util.Map;

import ar.com.fdvs.dj.domain.CustomExpression;
import ar.com.fdvs.dj.domain.entities.conditionalStyle.ConditionStyleExpression;

@SuppressWarnings("rawtypes")
public class BackgroundCondition extends ConditionStyleExpression implements CustomExpression {
	private static final long serialVersionUID = -1880229765157555598L;
	private String fieldName;
	private String colorValue;

	public BackgroundCondition(String fieldName, String colorValue) {
		this.fieldName = fieldName;
		this.colorValue = colorValue;
	}

	public Object evaluate( Map fields, Map variables, Map parameters) {
		boolean condition = false;
		Object currentValue = fields.get(fieldName);
		if (currentValue instanceof String) {
			String s = (String) currentValue;
			condition = colorValue.equals(currentValue);
		}
		return condition;
	}

	public String getClassName() {
		return Boolean.class.getName();
	}
}
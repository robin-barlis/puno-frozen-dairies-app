package com.example.application.reports;

public class Employee {
	
	private int id;
	private String name;
	private int testField1;
	private float floatField;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTestField1() {
		return testField1;
	}
	public void setTestField1(int testField1) {
		this.testField1 = testField1;
	}
	public float getFloatField() {
		return floatField;
	}
	public void setFloatField(float floatField) {
		this.floatField = floatField;
	}
	public Employee(int id, String name, int testField1, float floatField) {
		super();
		this.id = id;
		this.name = name;
		this.testField1 = testField1;
		this.floatField = floatField;
	}



}

package com.example.application;

public class IncorrectOrderException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public IncorrectOrderException(String message) {
		super(message);
	}

}

package com.example.application.data;

public enum ChequeStatus {
	
	RECEIVED("Received"), 
	CHEQUE_CLEARED("Cleared"), 
	CHEQUE_BOUNCED("Cheque Not Cleared");
	
	private String status;

	ChequeStatus(String status) {
		this.status = status;
	}
	
	public String getChequeStatus() {
		return status;
	}

}

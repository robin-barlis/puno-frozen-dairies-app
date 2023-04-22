package com.example.application.data;

public enum AccountsReportType {
    STATEMENT_OF_ACCOUNT("Statement Of Account");
	

	private String reportName;
	
	private AccountsReportType(String reportName) {
		this.reportName = reportName;
	}
	
	public String getReportName() {
		return reportName;
	}
}

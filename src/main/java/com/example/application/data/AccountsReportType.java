package com.example.application.data;

public enum AccountsReportType {
    STATEMENT_OF_ACCOUNT("Statement Of Account"),
    OUTSTANDING_CHEQUE_SUMMARY("Outstanding Cheque Summary"),
    ONLINE_PAYMENT_SUMMARY("Online Payment Summary"),
	SUBSIDIARY_LEDGER("Subsidiary Ledger");
	

	private String reportName;
	
	private AccountsReportType(String reportName) {
		this.reportName = reportName;
	}
	
	public String getReportName() {
		return reportName;
	}
}

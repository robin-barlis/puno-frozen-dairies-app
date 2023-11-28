package com.example.application.reports.datasource;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;

public class OrderSummaryDataSource implements JRDataSource, JRDataSourceProvider {

	@Override
	public Object getFieldValue(JRField arg0) throws JRException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean next() throws JRException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JRDataSource create(JasperReport arg0) throws JRException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose(JRDataSource arg0) throws JRException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JRField[] getFields(JasperReport arg0) throws JRException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsGetFieldsOperation() {
		// TODO Auto-generated method stub
		return false;
	}

}

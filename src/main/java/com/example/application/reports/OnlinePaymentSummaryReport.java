package com.example.application.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cloudinary.utils.StringUtils;
import com.example.application.data.PaymentMode;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.payment.BankRemittancePaymentDetails;
import com.example.application.data.entity.payment.ChequePaymentDetails;
import com.example.application.data.entity.payment.Payment;
import com.example.application.utils.PfdiUtil;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

@Service
public class OnlinePaymentSummaryReport {

	public byte[] buildReport(Map<String, List<Payment>> payments, Customer customer) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			getReportBuilder(payments, customer).toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	public JasperReportBuilder getReportBuilder(Map<String, List<Payment>> payments, Customer customer) {
		
		

		SubreportBuilder subreport = cmp.subreport(new SubreportExpression(payments))
				.setDataSource(new SubreportDataSourceExpression(payments));
		return report().title()
				.detail(subreport, cmp.verticalGap(20))
				.setDataSource(createDataSource(payments));
	}

	private JRDataSource createDataSource(Map<String, List<Payment>> payments) {
		
		if (Objects.isNull(payments) || payments.isEmpty()) {
			return new JREmptyDataSource(1);
		}

		return new JREmptyDataSource(1);
	}

	private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {
		private static final long serialVersionUID = 1L;

		private Map<String, List<Payment>> payments;

		public SubreportExpression(Map<String, List<Payment>> payments) {
			this.payments = payments;
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();
			JasperReportBuilder report = report();
			report.setTemplate(Templates.reportTemplate);
			
			if (!Objects.nonNull(payments) || payments.isEmpty()) {
				return report.noData(cmp.text("No Payments for the selected dates"));
			}

			if (1 == masterRowNumber) {

				report.title(cmp.text("Dealer's outstanding Cheque Summary"));

				TextColumnBuilder<String> dateColumn = col.column("Delivery Received", "receivedDateKey", type.stringType());
				TextColumnBuilder<String> dueDateColumn = col.column("Date Deposited", "depositDateKey",type.stringType());
				TextColumnBuilder<String> chequeNumberColumn = col.column("Account Number", "accountNumberKey", type.stringType());
				TextColumnBuilder<String> bankNameColumn = col.column("Bank Name", "bankNameChequeKey",type.stringType());
				TextColumnBuilder<String> dealersName = col.column("Dealer's Name", "dealersNameKey",type.stringType());
				TextColumnBuilder<String> addressColumn = col.column("Location", "locationKey",type.stringType());
				TextColumnBuilder<String> statusColumn = col.column("Status", "statusKey",type.stringType());
				TextColumnBuilder<BigDecimal> amountColumn = col.column("Amount", "amountChequeKey",type.bigDecimalType()).setStyle(Templates.columnRight);
				amountColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());

				report.addColumn(dateColumn, dueDateColumn, chequeNumberColumn, bankNameColumn,
						dealersName, addressColumn, amountColumn);
				report.subtotalsAtSummary(sbt.text("GRAND TOTAL", dateColumn), 
						sbt.text("", dueDateColumn), 
						sbt.text("", chequeNumberColumn),   
						sbt.text("", bankNameColumn),  
						sbt.text("", dealersName),  
						sbt.text("", addressColumn),
						sbt.text("",statusColumn),
						sbt.sum(amountColumn));
			}

			

			return report;
		}
	}

	private class SubreportDataSourceExpression extends AbstractSimpleExpression<JRDataSource> {
		private static final long serialVersionUID = 1L;

		private Map<String, List<Payment>> paymentsMap;

		public SubreportDataSourceExpression(Map<String, List<Payment>> paymentsMap) {
			this.paymentsMap = paymentsMap;
		}

		@Override
		public JRDataSource evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();

			if (1 == masterRowNumber) {

				List<Payment> payments = paymentsMap.get(PaymentMode.ONLINE_REMITTANCE.name());
							
				if (Objects.nonNull(payments)) {
					DRDataSource dataSource = new DRDataSource(
							"receivedDateKey", 
							"depositDateKey", 
							"accountNumberKey", 
							"bankNameChequeKey",
							"dealersNameKey",
							"locationKey",
							"statusKey",
							"amountChequeKey");

					for (Payment payment : payments) {
						
						BankRemittancePaymentDetails bankDetails = payment.getBankRemittanceDetails();

						dataSource.add(getStringValue(PfdiUtil.formatDate(payment.getCreatedDate())),
								getStringValue(bankDetails.getDepositDate()),
								getStringValue(bankDetails.getAccountNumber()),
								getStringValue(bankDetails.getBankId().getBankName()),
								getStringValue(payment.getCustomer().getOwnerName()),
								getStringValue(payment.getCustomer().getAddress()),
								getStringValue("DEPOSITED"),
								payment.getAmount());
					}
					return dataSource;

				}
			}

		

			return new DRDataSource("No Data");

		}
	}

	public Object getStringValue(Object object) {

		return object != null ? object.toString() : StringUtils.EMPTY;
	}

}
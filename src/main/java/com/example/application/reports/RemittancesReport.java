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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import com.cloudinary.utils.StringUtils;
import com.example.application.data.PaymentMode;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.payment.Payment;

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
public class RemittancesReport {

	public byte[] buildReport(Map<String, List<Payment>> payments, AppUser appUser) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			getReportBuilder(payments, appUser).toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	public JasperReportBuilder getReportBuilder(Map<String, List<Payment>> payments, AppUser appUser) {

		SubreportBuilder subreport = cmp.subreport(new SubreportExpression(payments)).setDataSource(new SubreportDataSourceExpression(payments));
		return report().title(Templates.createRemittancesDetailsComponent())
				.detail(subreport)
				.addLastPageFooter(Templates.createRemittancesDetailsFooterComponent(payments))
				.addLastPageFooter(Templates.createRemittancesPageFooterComponent(appUser))
				.setDataSource(createDataSource(payments));
	}

	private JRDataSource createDataSource(Map<String, List<Payment>> payments) {
		
		if (Objects.isNull(payments) || payments.isEmpty()) {
			return new JREmptyDataSource(1);
		}

		return new JREmptyDataSource(4);
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

				report.title(cmp.text("CASH PAYMENTS"));

				TextColumnBuilder<String> dateColumn = col.column("Delivery Date", "dateKey", type.stringType());
				TextColumnBuilder<String> soNumberColumn = col.column("S.O. Number", "soNumberKey", type.stringType());
				TextColumnBuilder<String> siNumberColumn = col.column("S.I. Number", "siNumberKey", type.stringType());
				TextColumnBuilder<String> accountNameColumn = col.column("Outlet/Store Name", "accountNameKey",type.stringType());
				TextColumnBuilder<BigDecimal> amountColumn = col.column("Amount", "amountKey", type.bigDecimalType()).setStyle(Templates.columnRight.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY)));
				amountColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());
				
				report.addColumn(dateColumn, soNumberColumn, siNumberColumn, accountNameColumn, amountColumn);
				report.subtotalsAtSummary(sbt.text("", dateColumn), 
						sbt.text("", soNumberColumn), 
						sbt.text("", siNumberColumn),  
						sbt.text("", accountNameColumn),  
						sbt.sum(amountColumn));
			}

			if (2 == masterRowNumber) {
				report.title(cmp.text("CASH BREAKDOWN"));
				TextColumnBuilder<String> denominationColumn = col.column("Denomination", "denominationKey",
						type.stringType());
				TextColumnBuilder<String> quantityColumn = col.column("Quantity", "quantityKey", type.stringType());
				TextColumnBuilder<String> totalColumn = col.column("Total", "totalKey", type.stringType());

				report.addColumn(denominationColumn, quantityColumn, totalColumn);

			}

			if (3 == masterRowNumber) {

				report.title(cmp.text("CHEQUE PAYMENT"));

				TextColumnBuilder<String> dateColumn = col.column("Delivery Date", "dateChequeKey", type.stringType());
				TextColumnBuilder<String> soNumberColumn = col.column("S.O. Number", "soNumberChequeKey",
						type.stringType());
				TextColumnBuilder<String> siNumberColumn = col.column("S.I. Number", "siNumberChequeKey",
						type.stringType());
				TextColumnBuilder<String> accountNameColumn = col.column("Account Name", "accountNameChequeKey",
						type.stringType());
				TextColumnBuilder<String> bankNameColumn = col.column("Bank Name", "bankNameChequeKey",
						type.stringType());
				TextColumnBuilder<String> chequeNoColumn = col.column("Cheque Number", "chequeNoChequeKey",
						type.stringType());
				TextColumnBuilder<String> paymentDateColumn = col.column("Cheque Due Date", "paymentDateChequeKey",
						type.stringType());
				TextColumnBuilder<BigDecimal> amountColumn = col.column("Amount", "amountChequeKey",
						type.bigDecimalType()).setStyle(Templates.columnRight);;
				amountColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());
						
				report.addColumn(dateColumn, soNumberColumn, siNumberColumn, accountNameColumn, bankNameColumn,
						chequeNoColumn, paymentDateColumn, amountColumn);
				report.subtotalsAtSummary(sbt.text("", dateColumn), 
						sbt.text("", soNumberColumn), 
						sbt.text("", siNumberColumn),  
						sbt.text("", accountNameColumn),  
						sbt.text("", bankNameColumn),  
						sbt.text("", chequeNoColumn),  
						sbt.text("", paymentDateColumn),  
						sbt.sum(amountColumn));

			}

			if (4 == masterRowNumber) {
				report.title(cmp.text("ONLINE REMITTANCE"));
				TextColumnBuilder<String> dateColumn = col.column("Delivery Date", "dateBankBankKey", type.stringType());
				TextColumnBuilder<String> soNumberColumn = col.column("S.O. Number", "soNumberBankKey",
						type.stringType());
				TextColumnBuilder<String> siNumberColumn = col.column("S.I. Number", "siNumberBankKey",
						type.stringType());
				TextColumnBuilder<String> accountNameColumn = col.column("Account Name", "accountNameBankKey",
						type.stringType());
				TextColumnBuilder<String> bankNameColumn = col.column("Bank Name", "bankNameBankKey",
						type.stringType());
				TextColumnBuilder<String> salesDateColumn = col.column("Sales Date", "salesDateBankKey",
						type.stringType());
				TextColumnBuilder<String> paymentDateColumn = col.column("Deposit Date", "paymentDateBankKey",
						type.stringType());
				TextColumnBuilder<BigDecimal> amountColumn = col.column("Amount", "amountBankKey",
						type.bigDecimalType()).setStyle(Templates.columnRight);
				amountColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());

				report.addColumn(dateColumn, soNumberColumn, siNumberColumn, accountNameColumn, bankNameColumn,
						paymentDateColumn, salesDateColumn, amountColumn);
				report.subtotalsAtSummary(sbt.text("", dateColumn), 
						sbt.text("", soNumberColumn), 
						sbt.text("", siNumberColumn),  
						sbt.text("", accountNameColumn),  
						sbt.text("", bankNameColumn),  
						sbt.text("", paymentDateColumn), 
						sbt.text("", salesDateColumn),   
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

				List<Payment> cashPayments = paymentsMap.get(PaymentMode.CASH.name());

				DRDataSource dataSource = new DRDataSource(
						"dateKey", 
						"soNumberKey", 
						"siNumberKey", 
						"accountNameKey",
						"amountKey");

				for (Payment payment : cashPayments) {

					dataSource.add(getStringValue(payment.getOrderId().getDeliveryDate()),
							getStringValue(payment.getOrderId().getStockOrderNumber()),
							getStringValue(payment.getOrderId().getInvoiceId()),
							getStringValue(payment.getOrderId().getCustomer().getOwnerName()),
							payment.getAmount());
				}
				return dataSource;
			}

			if (2 == masterRowNumber) {

				List<Payment> cashPayments = paymentsMap.get(PaymentMode.CASH.name());

				if (Objects.nonNull(cashPayments)) {
					DRDataSource dataSource = new DRDataSource("denominationKey", "quantityKey", "totalKey");

					for (String denomination : createCashBreakDownDataSource()) {

						dataSource.add(denomination, "", "");
					}
					dataSource.add("", "TOTAL AMOUNT:", "");
					return dataSource;

				}

			}
			
			if (3 == masterRowNumber) {

				List<Payment> payments = paymentsMap.get(PaymentMode.CHEQUE.name());

				if (Objects.nonNull(payments)) {
					DRDataSource dataSource = new DRDataSource(
							"dateChequeKey", 
							"soNumberChequeKey", 
							"siNumberChequeKey", 
							"accountNameChequeKey",
							"bankNameChequeKey",
							"chequeNoChequeKey",
							"paymentDateChequeKey",
							"amountChequeKey");

					for (Payment payment : payments) {

						dataSource.add(getStringValue(payment.getOrderId().getDeliveryDate()),
								getStringValue(payment.getOrderId().getStockOrderNumber()),
								getStringValue(payment.getOrderId().getInvoiceId()),
								getStringValue(payment.getOrderId().getCustomer().getOwnerName()),
								getStringValue(payment.getChequePaymentDetails().getBankId().getBankName()),
								getStringValue(payment.getChequePaymentDetails().getChequeNumber()),
								getStringValue(payment.getChequePaymentDetails().getChequeDueDate()),
								payment.getAmount());
					}
					return dataSource;

				}

			}
			
			if (4 == masterRowNumber) {

				List<Payment> payments = paymentsMap.get(PaymentMode.ONLINE_REMITTANCE.name());

				if (Objects.nonNull(payments)) {
					DRDataSource dataSource = new DRDataSource(
							"dateBankBankKey", 
							"soNumberBankKey", 
							"siNumberBankKey", 
							"accountNameBankKey",
							"bankNameBankKey",
							"paymentDateBankKey",
							"amountBankKey",
							"salesDateBankKey");

					for (Payment payment : payments) {
						try {
							dataSource.add(getStringValue(payment.getOrderId().getDeliveryDate()),
									getStringValue(payment.getOrderId().getStockOrderNumber()),
									getStringValue(payment.getOrderId().getInvoiceId()),
									getStringValue(payment.getOrderId().getCustomer().getOwnerName()),
									getStringValue(payment.getBankRemittanceDetails().getBankId().getBankName()),
									getStringValue(payment.getBankRemittanceDetails().getDepositDate()),
									payment.getAmount(),
									getStringValue(payment.getBankRemittanceDetails().getSalesDateCovered()));
							System.out.println("payment amount " + payment.getAmount() );
						} catch (Exception e) {
							e.printStackTrace();
						}
					
					}
					return dataSource;

				}

			}

			return new DRDataSource("No Data");

		}

		private List<String> createCashBreakDownDataSource() {
			ArrayList<String> denomination = Lists.newArrayList();
			denomination.add("PHP 1, 000.00");
			denomination.add("PHP 500.00");
			denomination.add("PHP 200.00");
			denomination.add("PHP 100.00");
			denomination.add("PHP 50.00");
			denomination.add("PHP 20.00");
			denomination.add("Coins");

			return denomination;
		}
	}

	public Object getStringValue(Object object) {

		return object != null ? object.toString() : StringUtils.EMPTY;
	}

}
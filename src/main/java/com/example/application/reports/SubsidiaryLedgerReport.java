package com.example.application.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;
import com.cloudinary.utils.StringUtils;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.Transaction;
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
public class SubsidiaryLedgerReport {

	public byte[] buildReport(List<Order> orders, Customer customer) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			getReportBuilder(orders, customer).toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	public JasperReportBuilder getReportBuilder(List<Order> orders, Customer customer) {
		
		

		SubreportBuilder subreport = cmp.subreport(new SubreportExpression(orders, customer))
				.setDataSource(new SubreportDataSourceExpression(orders));
		return report().title(Templates.createOutstandingChequeDetailsComponent(customer))
				.detail(subreport, cmp.verticalGap(20))
				.setDataSource(createDataSource(orders));
	}

	private JRDataSource createDataSource(List<Order> orders) {
		
		if (Objects.isNull(orders) || orders.isEmpty()) {
			return new JREmptyDataSource(1);
		}

		return new JREmptyDataSource(1);
	}

	private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {
		private static final long serialVersionUID = 1L;

		private List<Order> orders;
		private Customer customer;

		public SubreportExpression(List<Order> orders, Customer customer) {
			this.orders = orders;
			this.customer = customer;
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();
			JasperReportBuilder report = report();
			report.setTemplate(Templates.reportTemplateSmall);
			
			if (!Objects.nonNull(orders) || orders.isEmpty()) {
				return report.noData(cmp.text("No Orders for the selected dates"));
			}

			if (1 == masterRowNumber) {


				TextColumnBuilder<String> deliveryDateColumn = col.column("Date", "deliveryDateKey", type.stringType());
				TextColumnBuilder<String> stockOrderColumn = col.column("Stock Order", "stockOrderKey",type.stringType());
				TextColumnBuilder<String> salesInvoiceColumn = col.column("Sales Invoice", "salesInvoiceKey", type.stringType());
				TextColumnBuilder<String> drStColumn = col.column("Cheque Due Date", "chequeDueDateKey",type.stringType());
				TextColumnBuilder<String> particularsColumn = col.column("Particulars", "particularsKey",type.stringType());
				TextColumnBuilder<String> paymentTypeColumn = col.column("Payment Mode", "paymentModeKey",type.stringType());
				TextColumnBuilder<BigDecimal> amountGrossColumn = col.column("Amount Gross", "amountGrossKey",type.bigDecimalType()).setStyle(Templates.columnRight.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY)));
				TextColumnBuilder<BigDecimal> discountColumn = col.column("Discount", "discountKey",type.bigDecimalType()).setStyle(Templates.columnRight.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY)));
				TextColumnBuilder<BigDecimal> netCollection = col.column("Net Collection", "netCollectionKey",type.bigDecimalType()).setStyle(Templates.columnRight.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY)));
				TextColumnBuilder<BigDecimal> balanceColumn = col.column("Balance", "balanceKey",type.bigDecimalType()).setStyle(Templates.columnRight.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY)));
				amountGrossColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());
				discountColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());
				netCollection.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());
				balanceColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());
				
				
				report.addColumn(deliveryDateColumn, stockOrderColumn, salesInvoiceColumn, drStColumn,
						particularsColumn, paymentTypeColumn, amountGrossColumn,discountColumn, netCollection, balanceColumn);
				
				
			}
			return report;
		}
	}

	private class SubreportDataSourceExpression extends AbstractSimpleExpression<JRDataSource> {
		private static final long serialVersionUID = 1L;

		private List<Order> orders;

		public SubreportDataSourceExpression(List<Order> orders) {
			this.orders = orders;
		}

		@Override
		public JRDataSource evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();

			if (1 == masterRowNumber) {
	
				if (Objects.nonNull(orders)) {
						List<Transaction> transactions = Lists.newArrayList();
					for (Order order : orders) {
						
						Transaction orderTransaction = new Transaction(order);	
						
						transactions.add(orderTransaction);
						
						List<Payment> payments = order.getPayments();
						
						if (Objects.nonNull(payments)) {
							for (Payment payment : payments) {

								Transaction paymentTransaction = new Transaction(payment);
								transactions.add(paymentTransaction);
							}
						}
			
					}
					
					transactions.sort(PfdiUtil.getLocalDateComparator());

					DRDataSource dataSource = new DRDataSource(
							"deliveryDateKey", 
							"stockOrderKey", 
							"salesInvoiceKey", 
							"chequeDueDateKey",
							"particularsKey",
							"paymentModeKey",
							"amountGrossKey",
							"discountKey",
							"netCollectionKey", 
							"balanceKey");
					
					BigDecimal runningBalance = BigDecimal.ZERO;
					for (Transaction transaction : transactions) {
						BigDecimal amountDue = transaction.getAmountGross();
						BigDecimal discount = transaction.getDiscount();
						BigDecimal collections = transaction.getNetCollection();
						
						if (Objects.nonNull(amountDue)) {
							runningBalance = runningBalance.add(amountDue);
						}
						
						if (Objects.nonNull(discount)) {
							runningBalance = runningBalance.subtract(discount);
						}
						
						if (Objects.nonNull(collections)) {
							runningBalance = runningBalance.subtract(collections);
						}

						
						dataSource.add(getStringValue(PfdiUtil.formatDate(transaction.getDate())),
								getStringValue(transaction.getSoNumber()),
								getStringValue(transaction.getSiNumber()),
								getStringValue(transaction.getChequeDueDate()),
								getStringValue(transaction.getParticulars()),
								getStringValue(transaction.getPaymentMode()),
								transaction.getAmountGross(),
								transaction.getDiscount(),
								transaction.getNetCollection(),
								runningBalance);
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

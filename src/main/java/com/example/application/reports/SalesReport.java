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
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.cloudinary.utils.StringUtils;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
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
public class SalesReport {

	public byte[] buildReport(List<Order> orders) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			getReportBuilder(orders).toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	public JasperReportBuilder getReportBuilder(List<Order> orders) {
		
		

		SubreportBuilder subreport = cmp.subreport(new SubreportExpression(orders))
				.setDataSource(new SubreportDataSourceExpression(orders));
		return report().title()
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

		public SubreportExpression(List<Order> orders) {
			this.orders = orders;
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

				report.title(cmp.text("STOCK ORDER SUMMARY"));

				TextColumnBuilder<String> dateColumn = col.column("Date", "receivedDateKey", type.stringType());
				TextColumnBuilder<String> stockOrderColumn = col.column("Stock Order", "stockOrderKey",type.stringType());
				TextColumnBuilder<String> salesInvoiceColumn = col.column("Sales Invoice", "salesInvoiceKey", type.stringType());
				TextColumnBuilder<String> drStColumn = col.column("DR/ST", "drStKey",type.stringType());
				TextColumnBuilder<String> outletNameColumn = col.column("Outlet/Dealer", "outletNameKey",type.stringType());
				TextColumnBuilder<String> accountNameColumn = col.column("Account Name", "accountNameKey",type.stringType());
				TextColumnBuilder<String> tagColumn = col.column("Tag", "tagKey",type.stringType());
				TextColumnBuilder<BigDecimal> amountColumn = col.column("Amount", "amountKey",type.bigDecimalType()).setStyle(Templates.columnRight.setBottomBorder(stl.penThin().setLineColor(Color.LIGHT_GRAY)));
				amountColumn.getColumn().setTitleStyle(Templates.columnTitleStyleRight.build());

				report.addColumn(dateColumn, stockOrderColumn, salesInvoiceColumn, drStColumn,
						outletNameColumn, accountNameColumn,tagColumn, amountColumn);
				report.subtotalsAtSummary(sbt.text("GRAND TOTAL", dateColumn), 
						sbt.text("", stockOrderColumn), 
						sbt.text("", salesInvoiceColumn),   
						sbt.text("", drStColumn),  
						sbt.text("", outletNameColumn),  
						sbt.text("", accountNameColumn),
						sbt.text("",tagColumn),
						sbt.sum(amountColumn));
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
					DRDataSource dataSource = new DRDataSource(
							"receivedDateKey", 
							"stockOrderKey", 
							"salesInvoiceKey", 
							"drStKey",
							"outletNameKey",
							"accountNameKey",
							"tagKey",
							"amountKey");

					for (Order order : orders) {
						
						Customer customer = order.getCustomer();
						dataSource.add(getStringValue(PfdiUtil.formatDate(order.getCreationDate())),
								getStringValue(order.getStockOrderNumber()),
								getStringValue(order.getInvoiceId()),
								getStringValue(PfdiUtil.getSrOrDrString(order)),
								getStringValue(customer.getStoreName()),
								getStringValue(customer.getOwnerName()),
								getStringValue(PfdiUtil.getTagString(customer)),
								order.getAmountDue());
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
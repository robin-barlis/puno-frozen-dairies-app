package com.example.application.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.application.data.Categories;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.utils.PfdiUtil;
import com.google.common.collect.Lists;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

@Service
public class DeliveryReceiptReport {

	private Map<Object, List<OrderItems>> orderItemPerCategoryMap;

	public byte[] buildReport(Order order) {

		this.orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			report().title(Templates.createDeliveryReceiptHeaderComponent(order), 
					cmp.subreport(createSubreport()))
					.addPageFooter(Templates.createStockTransferDetailsFooterComponent())
					.pageFooter(Templates.footerComponent).toPdf(baos);


		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}
	
	public JasperReportBuilder getReportBuilder(Order order) {

		this.orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));
		JasperReportBuilder report = report().title(Templates.createStockTransferDetailsComponent(order), cmp.subreport(createSubreport()))
					.addPageFooter(Templates.createDeliveryReceiptDetailsFooterComponent())
					.pageFooter(Templates.footerComponent);

		return report;
	}

	private JasperReportBuilder createSubreport() {
		JasperReportBuilder report = report();
		
		TextColumnBuilder<String> productColumn = col.column("Flavor", "flavor", type.stringType());

		report.setTemplate(Templates.reportTemplate2)
				.columns(productColumn,
						col.column("Product", "productSize", type.stringType()),
						col.column("Transfer Price", "tp", type.stringType()),
						col.column("Quantity", "quantity", type.stringType()))
				.groupBy(productColumn)
				
				.setDataSource(createSubreportDataSource())
				.addColumnFooter(Templates.stockOrderFooter());

		return report;
	}
	

	private JRDataSource createSubreportDataSource() {
		DRDataSource dataSource = new DRDataSource("flavor", "productSize", "tp", "quantity");
		
		
		List<OrderItemData> regularFlavors = getRegularFlavors();
		for (OrderItemData item : regularFlavors) {
			
		
			dataSource.add(item.getProductName(), item.getSize(), item.getPrice(), item.getQuantity());
			
		}
		
		List<OrderItems> cones = orderItemPerCategoryMap.get(Categories.Cones.name());
		
		
		if (Objects.nonNull(cones)) {
			for (OrderItems item : orderItemPerCategoryMap.get(Categories.Cones.name())) {
				System.out.println(item.getItemInventory().getProduct().getProductName());
				dataSource.add(item.getItemInventory().getProduct().getProductName(),
						item.getItemInventory().getSize().getSizeName(),
						item.getProductPrice().toString(),
						item.getQuantity().toString());
				
			}
		}
		
		List<OrderItems> others = orderItemPerCategoryMap.get(Categories.Others.name());
		
		
		if (Objects.nonNull(others)) {
			for (OrderItems item : others) {
				System.out.println(item.getItemInventory().getProduct().getProductName());
				String productName = item.getItemInventory().getProduct().getProductName();
				dataSource.add(productName,
						item.getItemInventory().getSize().getSizeName(),
						item.getProductPrice().toString(),
						item.getQuantity().toString());
				
			}
		}
		
		return dataSource;
	}

	private List<OrderItemData> getRegularFlavors() {
		List<OrderItems> flavors = orderItemPerCategoryMap.get(Categories.Flavors.name());
		
		List<OrderItemData> orders = Lists.newArrayList();
		
		Map<String, List<OrderItems>> flavorPerCategory = flavors.stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryName()));
		
		
		List<String> sortedKeys= Lists.newArrayList(flavorPerCategory.keySet());
		Collections.sort(sortedKeys, PfdiUtil.categoryNameComparator);
		
		for (String key : sortedKeys) {

			List<OrderItems> flavorsValuePerCategory = flavorPerCategory.get(key);
			
				
				flavorsValuePerCategory.forEach(item-> {
					String flavor = 
							item.getItemInventory().getProduct().getProductName();
					OrderItemData orderData = new OrderItemData(flavor.toUpperCase(), 
							item.getQuantity().toString(), 
							item.getItemInventory().getSize().getSizeName(),
							item.getProductPrice().toString());
					orders.add(orderData);
				});
			
		}
		
		
		return orders;
	}	
	
	protected class OrderItemData {
		
		private String quantity;
		private String size;
		private String price;
		private String productName;
		
		
		public OrderItemData(String productName, String quantity, String size, String price) {
			this.price = price;
			this.size = size;
			this.quantity = quantity;
			this.productName = productName;
		}


		public String getPrice() {
			return price;
		}


		public String getQuantity() {
			return quantity;
		}


		public String getSize() {
			return size;
		}
		
		public String getProductName() {
			return productName;
		}
		
		
		
	}

}
package com.example.application.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class StockTransferReport {

	private Map<Object, List<OrderItems>> orderItemPerCategoryMap;

	public byte[] buildReport(Order order) {

		this.orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			report().title(Templates.createStockTransferDetailsComponent(order), cmp.subreport(createSubreport()))
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
					.addPageFooter(Templates.createStockTransferDetailsFooterComponent())
					.pageFooter(Templates.footerComponent);

		return report;
	}

	private JasperReportBuilder createSubreport() {
		JasperReportBuilder report = report();
		
		TextColumnBuilder<String> categoryColumn = col.column("Category", "category", type.stringType());
		report.setTemplate(Templates.reportTemplate2)
				.columns(col.column("Quantity", "quantity", type.stringType()),
						col.column("Size", "size", type.stringType()),
						categoryColumn)
				.setDataSource(createSubreportDataSource())
				.addColumnFooter(Templates.stockOrderFooter());

		return report;
	}
	

	private JRDataSource createSubreportDataSource() {
		DRDataSource dataSource = new DRDataSource("quantity", "size", "category");
		
		
		List<OrderItemData> regularFlavors = getRegularFlavors();
		for (OrderItemData item : regularFlavors) {
			
		
			dataSource.add(item.getQuantity(), 
					item.getSize(), 
					item.getCategory());
			
		}
		
		List<OrderItems> cones = orderItemPerCategoryMap.get(Categories.Cones.name());
		
		
		if (Objects.nonNull(cones)) {
			for (OrderItems item : orderItemPerCategoryMap.get(Categories.Cones.name())) {
				System.out.println(item.getItemInventory().getProduct().getProductName());
				dataSource.add(item.getQuantity().toString(), 
						item.getItemInventory().getSize().getSizeName(), 
						item.getItemInventory().getProduct().getProductName());
				
			}
		}
		
		List<OrderItems> others = orderItemPerCategoryMap.get(Categories.Others.name());
		
		
		if (Objects.nonNull(others)) {
			for (OrderItems item : others) {
				System.out.println(item.getItemInventory().getProduct().getProductName());
				dataSource.add(item.getQuantity().toString(), 
						item.getItemInventory().getSize().getSizeName(), 
						item.getItemInventory().getProduct().getProductName());
				
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
			if ("Regular Ice Cream".equalsIgnoreCase(key)) {
				
				Map<String, List<OrderItems>> regularFlavorsBySize = flavorsValuePerCategory.stream()
						.collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getSize().getSizeName()));
				collectRegularIceCream(regularFlavorsBySize).forEach(e-> {
					OrderItemData orderData = new OrderItemData(e.getQuantity(), e.getSize(), e.getCategory());
					orders.add(orderData);
				});
			} else {
				
				
				flavorsValuePerCategory.forEach(e-> {
					OrderItemData orderData = new OrderItemData(e.getQuantity().toString(), e.getItemInventory().getSize().getSizeName(), e.getItemInventory().getProduct().getProductName());
					orders.add(orderData);
				});
			}
		}
		
		
		return orders;
	}

	private List<OrderItemData> collectRegularIceCream(Map<String, List<OrderItems>> regularFlavorsBySize) {
		
		
		List<OrderItemData> orderItemDataRegularList = Lists.newArrayList();
		for (Entry<String, List<OrderItems>> regularFlavorSizeEntry : regularFlavorsBySize.entrySet()) {

			Integer quantity = 0;
			for (OrderItems item : regularFlavorSizeEntry.getValue()) {
				int currentQuantity = item.getQuantity();
				quantity = quantity + currentQuantity;

			}
			
			OrderItemData orderItemData = new OrderItemData(quantity.toString(), regularFlavorSizeEntry.getKey(), "Regular Ice Cream");
			orderItemDataRegularList.add(orderItemData);
		}
		
		return orderItemDataRegularList;
	}
	
	
	protected class OrderItemData {
		
		private String quantity;
		private String size;
		private String category;
		
		
		public OrderItemData(String quantity, String size, String category) {
			this.category = category;
			this.size = size;
			this.quantity = quantity;
		}


		public String getCategory() {
			return category;
		}


		public String getQuantity() {
			return quantity;
		}


		public String getSize() {
			return size;
		}
		
		
	}

}
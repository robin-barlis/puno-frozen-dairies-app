package com.example.application.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
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
public class SalesInvoiceReport {

	public byte[] buildReport(Order order) {

	 Map<Object, List<OrderItems>>  orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			report().title(Templates.createSalesInvoiceDetailsComponents(order), 
					cmp.subreport(createSubreport(order, orderItemPerCategoryMap)))
					.addPageFooter(Templates.signatureSalesInvoice())
					.pageFooter(Templates.footerComponent).toPdf(baos);


		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}
	
	public JasperReportBuilder getReportBuilder(Order order) {

		Map<Object, List<OrderItems>> orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));
		
		JasperReportBuilder report = report().title(Templates.createSalesInvoiceDetailsComponents(order), 
						cmp.subreport(createSubreport(order, orderItemPerCategoryMap)))
					.addPageFooter(Templates.signatureSalesInvoice())
					.pageFooter(Templates.footerComponent);

		return report;
	}

	private JasperReportBuilder createSubreport(Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		JasperReportBuilder report = report();

		buildReport(report, order, orderItemPerCategoryMap);

		return report;
	}
	

	private void buildReport(JasperReportBuilder report, Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		TextColumnBuilder<String> particularsColumn = col.column("Particulars", "particulars", type.stringType());
		TextColumnBuilder<String> quantityColumn = col.column("Quantity", "quantity", type.stringType());
		TextColumnBuilder<String> unitColumn =  col.column("Unit", "unit", type.stringType());
		TextColumnBuilder<BigDecimal>  priceColumn = col.column("Unit Price", "price", type.bigDecimalType());
		TextColumnBuilder<BigDecimal>  amount = col.column("Amount", "amount", type.bigDecimalType());
		report.setTemplate(Templates.reportTemplate2)
		.columns(quantityColumn,
				unitColumn,
				particularsColumn,
				priceColumn,
				amount)
		.subtotalsAtSummary(sbt.text("", quantityColumn), 
				sbt.text("", unitColumn), 
				sbt.text("", particularsColumn),  
				sbt.text("Total Amount Due:", priceColumn),  
				sbt.sum(amount))
	
		.setDataSource(createSubreportDataSource(order, orderItemPerCategoryMap))
		.addColumnFooter(Templates.stockOrderFooter());
		
	}

	private JRDataSource createSubreportDataSource(Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		DRDataSource dataSource = new DRDataSource( "quantity", "unit", "particulars",  "price",  "amount");
		
		
		List<OrderItemData> regularFlavors = getRegularFlavors(order, orderItemPerCategoryMap);
		
		for (OrderItemData item : regularFlavors) {
			
			System.out.println(item.getProductPrice());
			dataSource.add(
					item.getQuantity(),
					item.getSize(), 
					item.getCategory(), 
					item.getProductPrice(),
					item.getAmount());
			
		}
		
		List<OrderItems> cones = orderItemPerCategoryMap.get(Categories.Cones.name());
		
	
			
		if (Objects.nonNull(cones)) {
			Collections.sort(cones, (o1,o2) -> {
				return o1.getItemInventory().getProduct().getProductName().compareTo(o2.getItemInventory().getProduct().getProductName());
			});
			
			
			for (OrderItems item : orderItemPerCategoryMap.get(Categories.Cones.name())) {

				BigDecimal currentPrice = PfdiUtil.getTotalPrice(item, order.getCustomer().getCustomerTagId());
				dataSource.add(
						item.getQuantity().toString(), 
						item.getItemInventory().getSize().getSizeName(), 
						item.getItemInventory().getProduct().getProductName(),
						item.getProductPrice(),
						currentPrice);
				
			}
		}
		
		List<OrderItems> others = orderItemPerCategoryMap.get(Categories.Others.name());
		

		if (Objects.nonNull(others)) {
			Collections.sort(others, (o1,o2) -> {
				return o1.getItemInventory().getProduct().getProductName().compareTo(o2.getItemInventory().getProduct().getProductName());
			});
			for (OrderItems item : others) {
				System.out.println(item.getItemInventory().getProduct().getProductName());

				BigDecimal currentPrice = PfdiUtil.getTotalPrice(item, order.getCustomer().getCustomerTagId());
				dataSource.add(
						item.getQuantity().toString(), 
						item.getItemInventory().getSize().getSizeName(), 
						item.getItemInventory().getProduct().getProductName(),
						item.getProductPrice(),
						currentPrice);
				
			}
		}
		
		return dataSource;
	}

	private List<OrderItemData> getRegularFlavors(Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		List<OrderItems> flavors = orderItemPerCategoryMap.get(Categories.Flavors.name());
		
		List<OrderItemData> orders = Lists.newArrayList();
		
		Map<String, List<OrderItems>> flavorPerCategory = flavors.stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryName()));
		
		
		List<String> sortedKeys= Lists.newArrayList(flavorPerCategory.keySet());
		Collections.sort(sortedKeys, PfdiUtil.categoryNameComparator);
		
		for (String key : sortedKeys) {

			List<OrderItems> flavorsValuePerCategory = flavorPerCategory.get(key);
			

			
			Collections.sort(flavorsValuePerCategory, (o1, o2) -> {
				Integer sortingIndex1 = o1.getItemInventory().getProduct().getSortingIndex();
				Integer sortingIndex2 = o2.getItemInventory().getProduct().getSortingIndex();
				return sortingIndex1.compareTo(sortingIndex2);
			});
			if ("Regular Ice Cream".equalsIgnoreCase(key)) {
				Map<String, List<OrderItems>> regularFlavorsBySize = flavorsValuePerCategory.stream()
						.collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getSize().getSizeName()));
				orders.addAll(collectRegularIceCream(regularFlavorsBySize, order));

			} else {
				
				
				for (OrderItems orderItem : flavorsValuePerCategory) {
					BigDecimal currentPrice = PfdiUtil.getTotalPrice(orderItem, order.getCustomer().getCustomerTagId());
					OrderItemData orderData = new OrderItemData(orderItem.getQuantity().toString(), 
							orderItem.getItemInventory().getSize().getSizeName(),
							orderItem.getItemInventory().getProduct().getProductName(), 
							orderItem.getProductPrice(),
							currentPrice);
					orders.add(orderData);
				}
			}
		}
		
		
		return orders;
	}

	private List<OrderItemData> collectRegularIceCream(Map<String, List<OrderItems>> regularFlavorsBySize, Order order) {
		
		
		List<OrderItemData> orderItemDataRegularList = Lists.newArrayList();

		for (Entry<String, List<OrderItems>> regularFlavorSizeEntry : regularFlavorsBySize.entrySet()) {

			Integer quantity = 0;

			BigDecimal amount = BigDecimal.ZERO;
			BigDecimal unitPrice = BigDecimal.ZERO;
			for (OrderItems item : regularFlavorSizeEntry.getValue()) {
				int currentQuantity = item.getQuantity();
				quantity = quantity + currentQuantity;
				BigDecimal price = PfdiUtil.getTotalPrice(item, order.getCustomer().getCustomerTagId());
				
				amount = amount.add(price);
				unitPrice = item.getProductPrice();
			}
			
			OrderItemData orderItemData = new OrderItemData(quantity.toString(), 
					regularFlavorSizeEntry.getKey(),
					"Regular Ice Cream",
					unitPrice,
					amount);
			orderItemDataRegularList.add(orderItemData);
		}
		
		return orderItemDataRegularList;
	}
	
	
	protected class OrderItemData {
		
		private String quantity;
		private String unit;
		private String particulars;
		private BigDecimal unitPrice;
		private BigDecimal amount;
		
		
		public OrderItemData(String quantity, String size, String category, BigDecimal productPrice, BigDecimal amount) {
			this.particulars = category;
			this.unit = size;
			this.quantity = quantity;
			this.unitPrice = productPrice;
			this.amount = amount;
		}


		public String getCategory() {
			return particulars;
		}


		public String getQuantity() {
			return quantity;
		}


		public String getSize() {
			return unit;
		}
		

		public BigDecimal getProductPrice() {
			return unitPrice;
		}
		
		public BigDecimal getAmount() {
			return amount;
		}
		
		
	}

}
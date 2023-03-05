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
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.utils.PfdiUtil;
import com.google.common.collect.Lists;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

@Service
public class StockTransferReport {

	public byte[] buildReport(Order order) {

	 Map<Object, List<OrderItems>>  orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			report().title(Templates.createStockTransferDetailsComponent(order), 
					cmp.subreport(createSubreport(order, orderItemPerCategoryMap)))
					.addPageFooter(Templates.createStockTransferDetailsFooterComponent())
					.pageFooter(Templates.footerComponent).toPdf(baos);


		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}
	
	public JasperReportBuilder getReportBuilder(Order order) {

		Map<Object, List<OrderItems>> orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));
		
		JasperReportBuilder report = report().title(Templates.createStockTransferDetailsComponent(order), cmp.subreport(createSubreport(order, orderItemPerCategoryMap)))
					.addPageFooter(Templates.createStockTransferDetailsFooterComponent())
					.pageFooter(Templates.footerComponent);

		return report;
	}

	private JasperReportBuilder createSubreport(Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		JasperReportBuilder report = report();
		
		
		if (PfdiUtil.isCompanyOwned(order.getCustomer().getCustomerTagId())) {
			getCompanyOwnedReport(report,order, orderItemPerCategoryMap);
		} else if (PfdiUtil.isRelative(order.getCustomer().getCustomerTagId())) {
			getRelativeOwnedReport(report, order, orderItemPerCategoryMap);
		} else {
			buildOthersReport(report, order, orderItemPerCategoryMap);

		}
		
		return report;
	}
	

	private void buildOthersReport(JasperReportBuilder report, Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		TextColumnBuilder<String> categoryColumn = col.column("Category", "category", type.stringType());
		TextColumnBuilder<String> quantityColumn = col.column("Quantity", "quantity", type.stringType());
		TextColumnBuilder<String> sizeColumn =  col.column("Size", "size", type.stringType());
		TextColumnBuilder<BigDecimal>  priceColumn = col.column("Transfer Price", "price", type.bigDecimalType());
		report.setTemplate(Templates.reportTemplate2)
		.columns(categoryColumn,
				sizeColumn,
				quantityColumn,
				priceColumn
				)
		.subtotalsAtSummary(sbt.text("Total Transfer Price", categoryColumn), 
				sbt.text("", sizeColumn), 
				sbt.text("", quantityColumn),  
				sbt.sum(priceColumn))
	
		.setDataSource(createSubreportDataSource(order, orderItemPerCategoryMap))
		.addColumnFooter(Templates.stockOrderFooter());
		
	}

	private void getRelativeOwnedReport(JasperReportBuilder report, Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		TextColumnBuilder<String> categoryColumn = col.column("Category", "category", type.stringType());
		TextColumnBuilder<String> quantityColumn = col.column("Quantity", "quantity", type.stringType());
		TextColumnBuilder<String> sizeColumn =  col.column("Size", "size", type.stringType());
		//TextColumnBuilder<BigDecimal>  priceColumn = col.column("Transfer Price", "price", type.bigDecimalType());
		report.setTemplate(Templates.reportTemplate2)
		.columns(categoryColumn,
				sizeColumn,
				quantityColumn
				)
		.subtotalsAtSummary(sbt.text("Total Transfer Price", categoryColumn), 
				sbt.text("", sizeColumn), 
				sbt.text(PfdiUtil.getFormatter().format(order.getAmountDue()), quantityColumn))
	
		.setDataSource(createSubreportDataSource(order, orderItemPerCategoryMap))
		.addColumnFooter(Templates.stockOrderFooter());
		
	}

	private void getCompanyOwnedReport(JasperReportBuilder report, Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		TextColumnBuilder<String> categoryColumn = col.column("Category", "category", type.stringType());
		TextColumnBuilder<String> quantityColumn = col.column("Quantity", "quantity", type.stringType());
		TextColumnBuilder<String> sizeColumn =  col.column("Size", "size", type.stringType());
		TextColumnBuilder<BigDecimal>  priceColumn = col.column("Amount in SRP", "price", type.bigDecimalType());
		report.setTemplate(Templates.reportTemplate2)
		.columns(categoryColumn,
				sizeColumn,
				quantityColumn,
				priceColumn
				)
		.subtotalsAtSummary(sbt.text("Total Amount (SRP)", categoryColumn), 
				sbt.text("", sizeColumn), 
				sbt.text("", quantityColumn),  
				sbt.sum(priceColumn))
	
		.setDataSource(createSubreportDataSource(order, orderItemPerCategoryMap))
		.addColumnFooter(Templates.stockOrderFooter());
		
	}

	private JRDataSource createSubreportDataSource(Order order, Map<Object, List<OrderItems>> orderItemPerCategoryMap) {
		DRDataSource dataSource = new DRDataSource( "category", "size", "quantity", "price" );
		
		
		List<OrderItemData> regularFlavors = getRegularFlavors(order, orderItemPerCategoryMap);
		
		for (OrderItemData item : regularFlavors) {
			
			System.out.println(item.getProductPrice());
			dataSource.add(item.getCategory(), 
					item.getSize(), 
					item.getQuantity(), 
					item.getProductPrice());
			
		}
		
		List<OrderItems> cones = orderItemPerCategoryMap.get(Categories.Cones.name());
		
	
			
		if (Objects.nonNull(cones)) {
			Collections.sort(cones, (o1,o2) -> {
				return o1.getItemInventory().getProduct().getProductName().compareTo(o2.getItemInventory().getProduct().getProductName());
			});
			
			
			for (OrderItems item : orderItemPerCategoryMap.get(Categories.Cones.name())) {

				BigDecimal currentPrice = PfdiUtil.getTotalPrice(item, order.getCustomer().getCustomerTagId());
				dataSource.add(
						item.getItemInventory().getProduct().getProductName(),
						item.getItemInventory().getSize().getSizeName(), 
						item.getQuantity().toString(), 
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
						item.getItemInventory().getProduct().getProductName(),
						item.getItemInventory().getSize().getSizeName(), 
						item.getQuantity().toString(), 
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

			BigDecimal productPrice = BigDecimal.ZERO;
			for (OrderItems item : regularFlavorSizeEntry.getValue()) {
				int currentQuantity = item.getQuantity();
				quantity = quantity + currentQuantity;
				BigDecimal price = PfdiUtil.getTotalPrice(item, order.getCustomer().getCustomerTagId());
				
				productPrice = productPrice.add(price);
			}
			
			OrderItemData orderItemData = new OrderItemData(quantity.toString(), 
					regularFlavorSizeEntry.getKey(),
					"Regular Ice Cream",
					productPrice);
			orderItemDataRegularList.add(orderItemData);
		}
		
		return orderItemDataRegularList;
	}
	
	
	protected class OrderItemData {
		
		private String quantity;
		private String size;
		private String category;
		private BigDecimal productPrice;
		
		
		public OrderItemData(String quantity, String size, String category, BigDecimal productPrice) {
			this.category = category;
			this.size = size;
			this.quantity = quantity;
			this.productPrice = productPrice;
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
		

		public BigDecimal getProductPrice() {
			return productPrice;
		}
		
		
	}

}
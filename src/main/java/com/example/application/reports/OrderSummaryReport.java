package com.example.application.reports;


import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.application.data.Categories;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Size;
import com.example.application.utils.PfdiUtil;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;


@Service
public class OrderSummaryReport {

	public byte[] buildReport(Order order, Set<OrderItems> orderItems, List<Size> sizes, AppUser appUser,
			List<Category> categories) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			getReportBuilder(order, sizes, appUser, categories).toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	public JasperReportBuilder getReportBuilder(Order order, List<Size> sizes, AppUser appUser,
			List<Category> categoryList) {

		TreeSet<String> treeSet = new TreeSet<String>();
		treeSet.add(Categories.Flavors.name());
		treeSet.add(Categories.Cones.name());
		treeSet.add(Categories.Others.name());

		Map<String, List<OrderItems>> orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryName()));
		
		
		LinkedList<String> orderedCategories = Lists.newLinkedList();
		
		orderItemPerCategoryMap.forEach((key, value) -> {	
			orderedCategories.add(key);
		});
		
		orderedCategories.sort(PfdiUtil.categoryNameComparator);
		

		Map<String, Set<Size>> categorySizes = categoryList.stream()
				.collect(Collectors.toMap(Category::getCategoryName, Category::getSizeSet));

		SubreportBuilder subreport = cmp.subreport(new SubreportExpression(categorySizes, orderedCategories))
				.setDataSource(new SubreportDataSourceExpression(orderItemPerCategoryMap, categorySizes, orderedCategories));

		return report().title(Templates.createStockOrderDetailsComponent(order))
				.pageHeader(Templates.createStockOrderHeaderBand(order))
				.detail(subreport, cmp.verticalGap(20))
				.addLastPageFooter(Templates.createStockOrderDetailsFooterComponent(order, appUser))
				.setDataSource(createDataSource(orderedCategories)).pageFooter(Templates.footerComponent);
	}

	private JRDataSource createDataSource(List<String> categoryList) {
		return new JREmptyDataSource(categoryList.size());
	}

	private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {

		private static final long serialVersionUID = 1L;
		private Map<String, Set<Size>> categorySizes;
		private List<String> categoryList;

		public SubreportExpression(Map<String, Set<Size>> categorySizes, List<String> categoryList) {
			this.categorySizes = categorySizes;
			this.categoryList = categoryList;
		}

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber() - 1;
			JasperReportBuilder report = report();
			report.setTemplate(Templates.reportTemplate);
			
			String currentCategory = categoryList.get(masterRowNumber);
			
			List<Size> currentSizes = Lists.newArrayList(categorySizes.get(currentCategory));
			

			TextColumnBuilder<String> flavorColumn = col.column(currentCategory, "key-" + currentCategory, type.stringType());
			flavorColumn.setWidth(80);
			report.addColumn(flavorColumn);
			report.addSubtotalAtSummary(
					sbt.text("Total Quantity", flavorColumn).setStyle(Templates.boldStyle8Font));
			currentSizes.sort(PfdiUtil.sizeComparator);
			for (Size size : currentSizes) {
				TextColumnBuilder<Integer> sizeColumn = col.column(size.getSizeName(), "key" + size.getId(),
						type.integerType());
				sizeColumn.setWidth(20);
				report.addColumn(sizeColumn);
				report.addSubtotalAtSummary(sbt.sum(sizeColumn).setStyle(Templates.boldStyle8Font));
			}

			return report;
		}
	}

	private class SubreportDataSourceExpression extends AbstractSimpleExpression<JRDataSource> {
		private static final long serialVersionUID = 1L;

		private Map<String, List<OrderItems>> orderItemPerCategoryMap;
		private Map<String, Set<Size>> categorySizes;
		private List<String> categoryList;
		
		
		public SubreportDataSourceExpression(Map<String, List<OrderItems>> orderItemPerCategoryMap,
				Map<String, Set<Size>> categorySizes, List<String> categoryList) {
			this.orderItemPerCategoryMap = orderItemPerCategoryMap;
			this.categorySizes = categorySizes;
			this.categoryList = categoryList;
		}

		@Override
		public JRDataSource evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber() - 1;
			JasperReportBuilder report = report();
			report.setTemplate(Templates.reportTemplate);
			
			String currentCategory = categoryList.get(masterRowNumber);
			
			List<Size> currentSizes = Lists.newArrayList(categorySizes.get(currentCategory));
			
			
			List<OrderItems> currentCategoryItems = orderItemPerCategoryMap.get(currentCategory);

			Collections.sort(currentCategoryItems, (o1, o2) -> {
				Integer sortingIndex1 = o1.getItemInventory().getProduct().getSortingIndex();

				Integer sortingIndex2 = o2.getItemInventory().getProduct().getSortingIndex();
				if (sortingIndex1 == null) {
					sortingIndex1 = 0;
				}

				if (sortingIndex2 == null) {
					sortingIndex2 = 0;
				}
				return sortingIndex1.compareTo(sortingIndex2);
			});

			Map<String, List<OrderItems>> categoryItemsMap = currentCategoryItems.stream()
					.collect(Collectors.groupingBy(orderItem -> {
						return orderItem.getItemInventory().getProduct().getProductName();
					}, LinkedHashMap::new, Collectors.toList()));

			String[] columns = new String[currentSizes.size() + 1];
			DRDataSource dataSource = new DRDataSource(columns);
			for (Entry<String, List<OrderItems>> entry : categoryItemsMap.entrySet()) {

				Object[] values = new Object[currentSizes.size() + 1];
				columns[0] = "key-" + currentCategory;
				values[0] = entry.getKey();

				int index = 0;

				List<OrderItems> orderItems = entry.getValue();

				for (OrderItems items : orderItems) {
					index++;
					String key = "key" + items.getItemInventory().getSize().getId();
					Integer value = items.getQuantity();

					columns[index] = key;
					values[index] = value;

				}

				dataSource.add(values);
			}

			return dataSource;
			

//			if (1 == masterRowNumber) {
//
//				List<OrderItems> flavors = orderItemPerCategoryMap.get(Categories.Flavors.name());
//
//				Collections.sort(flavors, (o1, o2) -> {
//					Integer sortingIndex1 = o1.getItemInventory().getProduct().getSortingIndex();
//
//					Integer sortingIndex2 = o2.getItemInventory().getProduct().getSortingIndex();
//					if (sortingIndex1 == null) {
//						sortingIndex1 = 0;
//					}
//
//					if (sortingIndex2 == null) {
//						sortingIndex2 = 0;
//					}
//					return sortingIndex1.compareTo(sortingIndex2);
//				});
//
//				Map<String, List<OrderItems>> flavorItemsMap = flavors.stream()
//						.collect(Collectors.groupingBy(orderItem -> {
//							return orderItem.getItemInventory().getProduct().getProductName();
//						}, LinkedHashMap::new, Collectors.toList()));
//
//				String[] columns = new String[flavorSize.size() + 1];
//				DRDataSource dataSource = new DRDataSource(columns);
//				for (Entry<String, List<OrderItems>> entry : flavorItemsMap.entrySet()) {
//
//					Object[] values = new Object[flavorSize.size() + 1];
//					columns[0] = "keyFlavor";
//					values[0] = entry.getKey();
//
//					int index = 0;
//
//					List<OrderItems> orderItems = entry.getValue();
//
//					for (OrderItems items : orderItems) {
//						index++;
//						String key = "key" + items.getItemInventory().getSize().getId();
//						Integer value = items.getQuantity();
//
//						columns[index] = key;
//						values[index] = value;
//
//					}
//
//					dataSource.add(values);
//				}
//
//				return dataSource;
//			}
//
//			if (2 == masterRowNumber) {
//
//				List<OrderItems> cones = orderItemPerCategoryMap.get(Categories.Cones.name());
//
//				Collections.sort(cones, (o1, o2) -> {
//					Integer sortingIndex1 = o1.getItemInventory().getProduct().getSortingIndex();
//
//					Integer sortingIndex2 = o2.getItemInventory().getProduct().getSortingIndex();
//					if (sortingIndex1 == null) {
//						sortingIndex1 = 0;
//					}
//
//					if (sortingIndex2 == null) {
//						sortingIndex2 = 0;
//					}
//					return sortingIndex1.compareTo(sortingIndex2);
//				});
//
//				Map<String, List<OrderItems>> conesItemMap = cones.stream().collect(
//						Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getProductName()));
//				;
//
//				String[] columns = new String[coneSize.size() + 1];
//
//				DRDataSource dataSource = new DRDataSource(columns);
//				for (Entry<String, List<OrderItems>> entry : conesItemMap.entrySet()) {
//
//					Object[] values = new Object[coneSize.size() + 1];
//					columns[0] = "keyConeName";
//					int index = 0;
//
//					values[0] = entry.getKey();
//					List<OrderItems> orderItems = entry.getValue();
//
//					for (OrderItems items : orderItems) {
//
//						index++;
//						String key = "key" + items.getItemInventory().getSize().getId();
//						Integer value = items.getQuantity();
//						columns[index] = key;
//						values[index] = value;
//					}
//					dataSource.add(values);
//				}
//				return dataSource;
//			}
//
//			if (3 == masterRowNumber) {
//
//				List<OrderItems> others = orderItemPerCategoryMap.get(Categories.Others.name());
//
//				Collections.sort(others, (o1, o2) -> {
//					Integer sortingIndex1 = o1.getItemInventory().getProduct().getSortingIndex();
//
//					Integer sortingIndex2 = o2.getItemInventory().getProduct().getSortingIndex();
//					if (sortingIndex1 == null) {
//						sortingIndex1 = 0;
//					}
//
//					if (sortingIndex2 == null) {
//						sortingIndex2 = 0;
//					}
//					return sortingIndex1.compareTo(sortingIndex2);
//				});
//				Map<String, List<OrderItems>> othersItemMap = others.stream().collect(
//						Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getProductName()));
//				;
//
//				String[] columns = new String[othersSize.size() + 1];
//
//				DRDataSource dataSource = new DRDataSource(columns);
//				for (Entry<String, List<OrderItems>> entry : othersItemMap.entrySet()) {
//
//					Object[] values = new Object[othersSize.size() + 1];
//					columns[0] = "keyOthersName";
//					int index = 0;
//
//					values[0] = entry.getKey();
//
//					List<OrderItems> orderItems = entry.getValue();
//
//					for (OrderItems items : orderItems) {
//
//						index++;
//						String key = "key" + items.getItemInventory().getSize().getId();
//						Integer value = items.getQuantity();
//						columns[index] = key;
//						values[index] = value;
//					}
//
//					dataSource.add(values);
//				}
//
//				return dataSource;
//			}

			//return new DRDataSource("test");

		}
	}
}
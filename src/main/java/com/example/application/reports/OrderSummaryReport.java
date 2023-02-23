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
public class OrderSummaryReport {


	private Map<Object, List<OrderItems>> orderItemPerCategoryMap;
	private List<Size> flavorSize;
	private TreeSet<String> treeSet;
	private List<Size> coneSize;
	private List<Size> othersSize;

	public byte[] buildReport(Order order, Set<OrderItems> orderItems, List<Size> sizes, AppUser appUser) {



		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			getReportBuilder(order, orderItems, sizes, appUser).toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}
	
	public JasperReportBuilder getReportBuilder(Order order, Set<OrderItems> orderItems, List<Size> sizes, AppUser appUser) {


		this.flavorSize = sizes;
		this.treeSet = new TreeSet<String>();
		treeSet.add(Categories.Flavors.name());
		treeSet.add(Categories.Cones.name());
		treeSet.add(Categories.Others.name());

		this.orderItemPerCategoryMap = order.getOrderItems().stream().collect(Collectors
				.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryType()));

		this.flavorSize = sizes.stream().filter(e -> {
			Set<Category> categories = e.getCategory();

			return categories.stream()
					.anyMatch(catergory -> Categories.Flavors.name().equals(catergory.getCategoryType()));
		}).collect(Collectors.toList());
		Collections.sort(flavorSize, PfdiUtil.sizeComparator);
		
		this.coneSize = sizes.stream().filter(e -> {
			Set<Category> categories = e.getCategory();

			return categories.stream()
					.anyMatch(catergory -> Categories.Cones.name().equals(catergory.getCategoryType()));
		}).collect(Collectors.toList());
		
		this.othersSize = sizes.stream().filter(e -> {
			Set<Category> categories = e.getCategory();

			return categories.stream()
					.anyMatch(catergory -> Categories.Others.name().equals(catergory.getCategoryType()));
		}).collect(Collectors.toList());

		SubreportBuilder subreport = cmp.subreport(new SubreportExpression())
				.setDataSource(new SubreportDataSourceExpression());
		return report().title(Templates.createStockOrderDetailsComponent(order))
				.detail(subreport, cmp.verticalGap(20))
				.addPageFooter(Templates.createStockOrderDetailsFooterComponent(order, appUser))
				.setDataSource(createDataSource());
	}


	private JRDataSource createDataSource() {

		return new JREmptyDataSource(3);
	}

	private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {
		private static final long serialVersionUID = 1L;

		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();
			JasperReportBuilder report = report();
			report.setTemplate(Templates.reportTemplate);

			if (1 == masterRowNumber) {

				TextColumnBuilder<String> flavorColumn = col.column("Flavor", "keyFlavor", type.stringType());
				flavorColumn.setWidth(80);
				report.addColumn(flavorColumn);

				for (Size size : flavorSize) {
					System.out.println("Key: " + "key" + size.getId() + " Size name: " + size.getSizeName());

					System.out.println();
					TextColumnBuilder<String> sizeColumn = col.column(size.getSizeName(), "key" + size.getId(),
							type.stringType());
					sizeColumn.setWidth(20);
					report.addColumn(sizeColumn);
				}
			}
			
			if (2 == masterRowNumber) {

				TextColumnBuilder<String> flavorColumn = col.column("Cone", "keyConeName", type.stringType());
				flavorColumn.setWidth(80);
				report.addColumn(flavorColumn);

				for (Size size : coneSize) {
					System.out.println("Key: " + "key" + size.getId());
					TextColumnBuilder<String> sizeColumn = col.column(size.getSizeName(), "key" + size.getId(),
							type.stringType());
				//	sizeColumn.setWidth(20);
					report.addColumn(sizeColumn);
				}
			}
			
			if (3 == masterRowNumber) {

				TextColumnBuilder<String> flavorColumn = col.column("Cone", "keyOthersName", type.stringType());
				flavorColumn.setWidth(80);
				report.addColumn(flavorColumn);

				for (Size size : othersSize) {
					System.out.println("Key: " + "key" + size.getId());
					
					TextColumnBuilder<String> sizeColumn = col.column(size.getSizeName(), "key" + size.getId(),
							type.stringType());
					//sizeColumn.setWidth(20);
					report.addColumn(sizeColumn);
				}
			}

			return report;
		}
	}

	private class SubreportDataSourceExpression extends AbstractSimpleExpression<JRDataSource> {
		private static final long serialVersionUID = 1L;

		@Override
		public JRDataSource evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();
			
			if (1 == masterRowNumber) {

				List<OrderItems> flavors = orderItemPerCategoryMap.get(Categories.Flavors.name());
				
				Map<String, List<OrderItems>> flavorItemsMap = flavors.stream()
						.collect(Collectors.groupingBy(orderItem 
								-> orderItem.getItemInventory().getProduct().getProductName()));;
	
				

				String[] columns = new String[flavorSize.size()+1];
				DRDataSource dataSource = new DRDataSource(columns);
				for (Entry<String, List<OrderItems>> entry : flavorItemsMap.entrySet()) {
	
					Object[] values = new Object[flavorSize.size()+1];			
					columns[0] = "keyFlavor";
					values[0] = entry.getKey();
					
					int index = 0;
	

					for (OrderItems items : entry.getValue()) {
						index++;
						String key = "key" + items.getItemInventory().getSize().getId();
						System.out.println("key id: " + key);
						System.out.println("key id size name: " + items.getItemInventory().getSize().getSizeName() + " quantity : " + items.getQuantity().toString());
						String value = items.getQuantity().toString();
				
						columns[index] = key;
						values[index] = value;
	
					}
	
					dataSource.add(values);
				}
	
	
				return dataSource;
			}
			
			
			if (2 == masterRowNumber) {

				List<OrderItems> cones = orderItemPerCategoryMap.get(Categories.Cones.name());
				
				Map<String, List<OrderItems>> conesItemMap = cones.stream()
						.collect(Collectors.groupingBy(orderItem 
								-> orderItem.getItemInventory().getProduct().getProductName()));;
	
				String[] columns = new String[coneSize.size()+1];
	
	
				DRDataSource dataSource = new DRDataSource(columns);
				for (Entry<String, List<OrderItems>> entry : conesItemMap.entrySet()) {
	
					Object[] values = new Object[coneSize.size()+1];			
					columns[0] = "keyConeName";
					int index = 0;
	
					values[0] = entry.getKey();
					for (OrderItems items : entry.getValue()) {
	
						index++;
						String key = "key" + items.getItemInventory().getSize().getId();
						String value = items.getQuantity().toString();
						columns[index] = key;
						values[index] = value;
					}
					dataSource.add(values);
				}
				return dataSource;
			}
			
			if (3 == masterRowNumber) {

				List<OrderItems> others = orderItemPerCategoryMap.get(Categories.Others.name());
				

				Map<String, List<OrderItems>> othersItemMap = others.stream()
						.collect(Collectors.groupingBy(orderItem 
								-> orderItem.getItemInventory().getProduct().getProductName()));;
	
				String[] columns = new String[othersSize.size()+1];
	
	
				DRDataSource dataSource = new DRDataSource(columns);
				for (Entry<String, List<OrderItems>> entry : othersItemMap.entrySet()) {
	
					Object[] values = new Object[othersSize.size()+1];			
					columns[0] = "keyOthersName";
					int index = 0;
	
					values[0] = entry.getKey();
					for (OrderItems items : entry.getValue()) {
	
						index++;
						String key = "key" + items.getItemInventory().getSize().getId();
						String value = items.getQuantity().toString();
						columns[index] = key;
						values[index] = value;
					}
	
					dataSource.add(values);
				}
	
	
				return dataSource;
			}
			
			return new DRDataSource("test");
			

		}
	}

}
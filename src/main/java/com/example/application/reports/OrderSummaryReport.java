package com.example.application.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.application.data.Categories;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Size;
import com.example.application.utils.PfdiUtil;
import com.google.common.collect.Maps;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.HorizontalBandAlignment;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Stretching;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;


@Service
public class OrderSummaryReport {

	protected JasperPrint jp;
	protected JasperReport jr;
	protected DynamicReport dr;
	private Order order;
	private Set<OrderItems> orderItems;
	private List<Size> sizes;
	ArrayList<HashMap<String, String>> rowsDataList;
	

	public byte[] buildReport(Order order, Set<OrderItems> orderList, List<Size> sizes) throws ColumnBuilderException, ClassNotFoundException, JRException {
		this.order = order;
		this.orderItems= orderList;
		
		this.sizes = sizes.stream().filter(e -> {
			Set<Category> categories = e.getCategory();
			
			
			return categories.stream().anyMatch(catergory-> Categories.Flavors.name().equals(catergory.getCategoryType()));
		}).collect(Collectors.toList());
		

		rowsDataList = Lists.newArrayList();
		
		Set<OrderItems> flavorOrders = order.getOrderItems().stream().filter(oi -> {
			return Categories.Flavors.name().equals(oi.getItemInventory().getProduct().getCategory().getCategoryType());
		}).collect(Collectors.toSet());
		
		
		Map<String, List<OrderItems>> orderItemsMap = flavorOrders.stream()
				//.filter(e-> e.getItemInventory().getSize().getCategory().stream().anyMatch(cat -> Categories.Flavors.name().equals(cat.getCategoryName())))
				.collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getProductName()));;
		
				
		for (Entry<String, List<OrderItems>> entry : orderItemsMap.entrySet()) {
			HashMap<String, String> data = Maps.newHashMap();
			
			data.put("keyFlavor",entry.getKey());
			
			for (OrderItems items : entry.getValue()) {
				String key = "key" + items.getItemInventory().getSize().getId();
				String value = items.getQuantity().toString();
				data.put(key, value);
			}
			rowsDataList.add(data);
		}
		JasperPrint jp = getReport();
		
		byte[] pdfBytes = JasperExportManager.exportReportToPdf(jp);

		return pdfBytes;
	}


	private JasperPrint getReport()
			throws JRException, ColumnBuilderException, ClassNotFoundException {
		Style headerStyle = createHeaderStyle();
		Style detailTextStyle = createDetailTextStyle();
		Style detailNumberStyle = createDetailNumberStyle();
		DynamicReport dynaReport = getReport(headerStyle, detailTextStyle, detailNumberStyle);
		JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dynaReport, new ClassicLayoutManager(), new JRBeanCollectionDataSource(rowsDataList));
		return jp;
	}

	private Style createHeaderStyle() {
		StyleBuilder sb = new StyleBuilder(true);

		sb.setBorderBottom(Border.PEN_2_POINT());
		sb.setHorizontalAlign(HorizontalAlign.LEFT);
		sb.setVerticalAlign(VerticalAlign.BOTTOM);
		sb.setPaddingLeft(5);
		sb.setPaddingRight(1);
		sb.setTransparency(Transparency.OPAQUE);
		sb.setStretching(Stretching.RELATIVE_TO_BAND_HEIGHT);
		sb.setFont(Font.ARIAL_MEDIUM_BOLD);
		return sb.build();
	}

	private Style createDetailTextStyle() {
		StyleBuilder sb = new StyleBuilder(true);
		//sb.setFont(Font.VERDANA_MEDIUM);
		sb.setBorderBottom(Border.THIN());
		sb.setHorizontalAlign(HorizontalAlign.LEFT);
		sb.setVerticalAlign(VerticalAlign.MIDDLE);
		sb.setPaddingLeft(5);
		sb.setStretching(Stretching.RELATIVE_TO_BAND_HEIGHT);
		return sb.build();
	}

	private Style createDetailNumberStyle() {
		StyleBuilder sb = new StyleBuilder(true);
		sb.setBorderBottom(Border.THIN());
		
		sb.setHorizontalAlign(HorizontalAlign.LEFT);
		sb.setVerticalAlign(VerticalAlign.MIDDLE);
		sb.setPaddingRight(5);

		sb.setStretching(Stretching.RELATIVE_TO_BAND_HEIGHT);
		return sb.build();
	}

	private AbstractColumn createColumn(String property, Class type, String title, int width, Style headerStyle,
			Style detailStyle) throws ColumnBuilderException {
		AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(property, type.getName()).setTitle(title)
				.setWidth(Integer.valueOf(width))
				.setStyle(detailStyle)
				.setHeaderStyle(headerStyle)
				.build();
		return columnState;
	}

	private DynamicReport getReport(Style headerStyle, Style detailTextStyle, Style detailNumStyle)
			throws ColumnBuilderException, ClassNotFoundException {

		DynamicReportBuilder report = new DynamicReportBuilder();

		AbstractColumn flavorColumn = createColumn("keyFlavor", String.class, "Flavor", 140, headerStyle,
				detailTextStyle);
		
		report.addColumn(flavorColumn);
		for (Size size : sizes) {
			AbstractColumn column = createColumn("key" + size.getId(), String.class, size.getSizeName(), 55, headerStyle,
					detailTextStyle);
			
			report.addColumn(column);
		}
		
		StyleBuilder titleStyle = new StyleBuilder(true);
		titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);

		StyleBuilder subTitleStyle = new StyleBuilder(true);
		subTitleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
		
		AutoText title = new AutoText("Stock Order #" + order.getStockOrderNumber(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		title.setHeight(50);
		
		AutoText storeName = new AutoText("Store Name : " + order.getCustomer().getStoreName(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		storeName.setWidth(500);
		
		AutoText orderDate = new AutoText(PfdiUtil.formatDateWithHours(order.getCreationDate()),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.RIGHT);
		orderDate.setWidth(500);
		
		
		AutoText owner = new AutoText("Store Address : " + order.getCustomer().getAddress(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		owner.setWidth(500);
		
		AutoText address = new AutoText("Owner Name : " + order.getCustomer().getOwnerName(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		address.setWidth(500);	
		
		
		report.addAutoText(title);
		report.addAutoText(orderDate);
		report.addAutoText(storeName);
		report.addAutoText(address);
		report.addAutoText(owner);
		report.setTitleStyle(titleStyle.build());
		report.setSubtitleStyle(subTitleStyle.build());
		report.setUseFullPageWidth(true);
		
		
		
		
		return report.build();
	}

}

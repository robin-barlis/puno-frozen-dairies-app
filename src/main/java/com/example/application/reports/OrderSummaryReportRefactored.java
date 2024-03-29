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
import com.example.application.data.entity.products.Size;
import com.example.application.utils.PfdiUtil;
import com.google.common.collect.Maps;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.HorizontalBandAlignment;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.DJBuilderException;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.builders.SubReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Stretching;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.Subreport;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@SuppressWarnings("deprecation")
@Service
public class OrderSummaryReportRefactored {

	protected JasperPrint jp;
	protected JasperReport jr;
	protected DynamicReport dr;
	private Order order;

	private List<Size> coneSizes;
	ArrayList<HashMap<String, String>> rowsDataList;

	public byte[] buildReport(Order order, Set<OrderItems> orderList, List<Size> sizes)
			throws ColumnBuilderException, ClassNotFoundException, JRException {
		this.order = order;


		rowsDataList = Lists.newArrayList();

		Set<OrderItems> flavorOrders = order.getOrderItems().stream().filter(oi -> {
			return Categories.Flavors.name().equals(oi.getItemInventory().getProduct().getCategory().getCategoryType());
		}).collect(Collectors.toSet());

		Map<String, List<OrderItems>> orderItemsMap = flavorOrders.stream().collect(
				Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getProductName()));

		for (Entry<String, List<OrderItems>> entry : orderItemsMap.entrySet()) {
			HashMap<String, String> data = Maps.newHashMap();

			data.put("keyFlavor", entry.getKey());

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

	private JasperPrint getReport() throws JRException, ColumnBuilderException, ClassNotFoundException {
		Style headerStyle = createHeaderStyle();
		Style detailTextStyle = createDetailTextStyle();
		Style detailNumberStyle = createDetailNumberStyle();
		DynamicReport dynaReport = getReport(headerStyle, detailTextStyle, detailNumberStyle);

		JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dynaReport, new ClassicLayoutManager(),
				new JRBeanCollectionDataSource(rowsDataList));
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
		return sb.build();
	}

	private Style createDetailTextStyle() {
		StyleBuilder sb = new StyleBuilder(true);
		// sb.setFont(Font.VERDANA_MEDIUM);
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

	private AbstractColumn createColumn(String property, @SuppressWarnings("rawtypes") Class type, String title, int width, Style headerStyle,
			Style detailStyle) throws ColumnBuilderException {
		AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(property, type.getName()).setTitle(title)
				.setWidth(Integer.valueOf(width)).setStyle(detailStyle).setHeaderStyle(headerStyle).build();
		return columnState;
	}

	private DynamicReport getReport(Style headerStyle, Style detailTextStyle, Style detailNumStyle)
			throws ColumnBuilderException, ClassNotFoundException {

		FastReportBuilder report = new FastReportBuilder();
		report.addGroups(1);

//		AbstractColumn flavorColumn = createColumn("keyFlavor", String.class, "Flavor", 140, headerStyle,
//				detailTextStyle);
//
//		report.addColumn(flavorColumn);
//		for (Size size : flavorSizes) {
//			AbstractColumn column = createColumn("key" + size.getId(), String.class, size.getSizeName(), 55,
//					headerStyle, detailTextStyle);
//
//			report.addColumn(column);
//		}

		StyleBuilder titleStyle = new StyleBuilder(true);
		titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);

		StyleBuilder subTitleStyle = new StyleBuilder(true);
		subTitleStyle.setHorizontalAlign(HorizontalAlign.LEFT);

		AutoText title = new AutoText("Stock Order #" + order.getStockOrderNumber(), AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		title.setHeight(50);

		AutoText storeName = new AutoText("Store Name : " + order.getCustomer().getStoreName(),
				AutoText.POSITION_HEADER, HorizontalBandAlignment.LEFT);
		storeName.setWidth(500);

		AutoText orderDate = new AutoText(PfdiUtil.formatDateWithHours(order.getCreationDate()),
				AutoText.POSITION_HEADER, HorizontalBandAlignment.RIGHT);
		orderDate.setWidth(500);

		AutoText owner = new AutoText("Store Address : " + order.getCustomer().getAddress(), AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		owner.setWidth(500);

		AutoText address = new AutoText("Owner Name : " + order.getCustomer().getOwnerName(), AutoText.POSITION_HEADER,
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

		Subreport conesReport = getConesReport(headerStyle, detailTextStyle, detailNumStyle);
		report.addSubreportInGroupFooter(1, conesReport);

		return report.build();
	}

	private Subreport getConesReport(Style headerStyle, Style detailTextStyle, Style detailNumberStyle) throws ColumnBuilderException, DJBuilderException, ClassNotFoundException {
		Subreport subreport = new SubReportBuilder().setDataSource(DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
				DJConstants.DATA_SOURCE_TYPE_COLLECTION, "conesKey")
				.setDynamicReport(createConeSubreport( headerStyle,  detailTextStyle,  detailNumberStyle), new ClassicLayoutManager()).build();
		return subreport;
	}

	private DynamicReport createConeSubreport(Style headerStyle, Style detailTextStyle, Style detailNumberStyle) throws ColumnBuilderException, ClassNotFoundException {
		FastReportBuilder rb = new FastReportBuilder();

		
		AbstractColumn flavorColumn = createColumn("keyConeName", String.class, "Cone", 140, headerStyle,
				detailTextStyle);

		rb.addColumn(flavorColumn);
		for (Size size : coneSizes) {
			AbstractColumn column = createColumn("key" + size.getId(), String.class, size.getSizeName(), 55,
					headerStyle, detailTextStyle);

			rb.addColumn(column);
		}
		return rb.build();
	}
	
//	private DynamicReport createFlavorSubreport(Style headerStyle, Style detailTextStyle, Style detailNumberStyle) throws ColumnBuilderException, ClassNotFoundException {
//		FastReportBuilder rb = new FastReportBuilder();
//
//		
//		AbstractColumn flavorColumn = createColumn("keyFlavorName", String.class, "Flavor", 140, headerStyle,
//				detailTextStyle);
//
//		rb.addColumn(flavorColumn);
//		for (Size size : flavorSizes) {
//			AbstractColumn column = createColumn("key" + size.getId(), String.class, size.getSizeName(), 55,
//					headerStyle, detailTextStyle);
//
//			rb.addColumn(column);
//		}
//		return rb.build();
//	}
//	
//	private DynamicReport createOtherSubreport(Style headerStyle, Style detailTextStyle, Style detailNumberStyle) throws ColumnBuilderException, ClassNotFoundException {
//		FastReportBuilder rb = new FastReportBuilder();
//
//		
//		AbstractColumn flavorColumn = createColumn("keyOtherName", String.class, "Others", 140, headerStyle,
//				detailTextStyle);
//
//		rb.addColumn(flavorColumn);
//		for (Size size : otherSizes) {
//			AbstractColumn column = createColumn("key" + size.getId(), String.class, size.getSizeName(), 55,
//					headerStyle, detailTextStyle);
//
//			rb.addColumn(column);
//		}
//		return rb.build();
//	}

}

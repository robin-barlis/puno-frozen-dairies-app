package com.example.application.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItemSalesInvoiceWrapper;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.utils.PfdiUtil;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.HorizontalBandAlignment;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJCrosstab;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.CrosstabBuilder;
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
public class SalesInvoiceReport {

	protected JasperPrint jp;
	protected JasperReport jr;
	protected DynamicReport dr;
	private Order order;
	ArrayList<HashMap<String, String>> rowsDataList;
	private List<OrderItemSalesInvoiceWrapper> orderItemSalesInvoiceWrapperList;
	

	public byte[] buildReport(Order order) throws ColumnBuilderException, ClassNotFoundException, JRException {
		this.order = order;
		
		
		//TODO Extract this to service
		rowsDataList = Lists.newArrayList();
		
		Map<String, List<OrderItems>> flavorsItemMap = order.getOrderItems().stream()
				.filter(e -> "Regular Ice Cream".equalsIgnoreCase(e.getItemInventory().getProduct().getCategory().getCategoryName()))
				.collect(Collectors.groupingBy(
						orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryName()));

		Map<String, List<OrderItems>> regularFlavors = flavorsItemMap.get("Regular Ice Cream").stream()
				.collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getSize().getSizeName()));

		// Collect Regular Ice Cream Details
		orderItemSalesInvoiceWrapperList = Lists.newArrayList();
		for (Entry<String, List<OrderItems>> regularFlavorSizeEntry : regularFlavors.entrySet()) {
			OrderItemSalesInvoiceWrapper regularFlavorWrapper = new OrderItemSalesInvoiceWrapper();
			regularFlavorWrapper.setUnit(regularFlavorSizeEntry.getKey());

			int quantity = 0;
			BigDecimal unitPrice = new BigDecimal(0);
			for (OrderItems item : regularFlavorSizeEntry.getValue()) {
				int currentQuantity = item.getQuantity();
				quantity = quantity + currentQuantity;
				unitPrice = item.getProductPrice();

			}

			BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));

			regularFlavorWrapper.setAmount(totalAmount);
			regularFlavorWrapper.setUnitPrice(unitPrice);
			regularFlavorWrapper.setParticular("Regular Ice Cream");
			regularFlavorWrapper.setQuantity(quantity);
			orderItemSalesInvoiceWrapperList.add(regularFlavorWrapper);
		}
		
		List<OrderItems> otherFlavorsMapBySize = order.getOrderItems().stream()
				.filter(e -> {
					return !"Regular Ice Cream".equalsIgnoreCase(e.getItemInventory().getProduct().getCategory().getCategoryName());
				})
				.collect(Collectors.toList());

		// Collect Regular Ice Cream Details
		for (OrderItems orderItem : otherFlavorsMapBySize) {
			OrderItemSalesInvoiceWrapper orderItemWrapper = new OrderItemSalesInvoiceWrapper();
			orderItemWrapper.setUnit(orderItem.getItemInventory().getSize().getSizeName());
			
			BigDecimal unitPrice = orderItem.getProductPrice();
			int quantity = orderItem.getQuantity();
			orderItemWrapper.setAmount(unitPrice.multiply(new BigDecimal(quantity)));
			orderItemWrapper.setUnitPrice(unitPrice);
			orderItemWrapper.setParticular(orderItem.getItemInventory().getProduct().getProductName());
			orderItemWrapper.setQuantity(quantity);
			orderItemSalesInvoiceWrapperList.add(orderItemWrapper);
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
		JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dynaReport, new ClassicLayoutManager(), new JRBeanCollectionDataSource(orderItemSalesInvoiceWrapperList));
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
		
		setReportHeader(report);
		setReportOwnerDetails(report);
		
	
		AbstractColumn quantity = createColumn("quantity", Integer.class, "Quantity", 50, headerStyle, detailTextStyle);
		AbstractColumn unit = createColumn("unit", String.class, "Unit", 50, headerStyle, detailTextStyle);
		AbstractColumn particulars = createColumn("particular", String.class, "Particulars", 50, headerStyle, detailTextStyle);
		AbstractColumn unitPrice = createColumn("unitPrice", BigDecimal.class, "Unit Price", 50, headerStyle, detailTextStyle);
		AbstractColumn amount = createColumn("amount", BigDecimal.class, "Amount", 50, headerStyle, detailTextStyle);
		report.addColumn(quantity);
		report.addColumn(unit);
		report.addColumn(particulars);
		report.addColumn(unitPrice);
		report.addColumn(amount);
		
		StyleBuilder titleStyle = new StyleBuilder(true);
		titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);

		StyleBuilder subTitleStyle = new StyleBuilder(true);
		subTitleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
		
		setReportFooter(report);
		report.setTitleStyle(titleStyle.build());
		report.setSubtitleStyle(subTitleStyle.build());
		report.setUseFullPageWidth(true);
		
		return report.build();
	}

	private void setReportOwnerDetails(DynamicReportBuilder report) {
		
		StyleBuilder subheaderStyle = new StyleBuilder(true);
		subheaderStyle.setHorizontalAlign(HorizontalAlign.LEFT);
		
		Customer customer = order.getCustomer();
		
		AutoText labelOwnerAddress = new AutoText("Address: " + customer.getAddress(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		labelOwnerAddress.setWidth(250);
		labelOwnerAddress.setStyle(subheaderStyle.build());
		
		AutoText ownerNameLabel = new AutoText("Owner Name: " + customer.getOwnerName(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		ownerNameLabel.setWidth(250);
		ownerNameLabel.setStyle(subheaderStyle.build());
		
		AutoText tinNumberLabel = new AutoText("TIN Number: " + customer.getTinNumber(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		tinNumberLabel.setWidth(250);
		tinNumberLabel.setStyle(subheaderStyle.build());
		
		
		AutoText terms = new AutoText("Terms: _______________________________", 
				AutoText.POSITION_HEADER, 
				HorizontalBandAlignment.RIGHT);
		terms.setWidth(800);
		terms.setStyle(subheaderStyle.build());
		
		AutoText oscas = new AutoText("OSCA/PWD ID No.:: ____________________", 
				AutoText.POSITION_HEADER, 
				HorizontalBandAlignment.RIGHT);
		oscas.setWidth(800);
		oscas.setStyle(subheaderStyle.build());
		
		AutoText cardh = new AutoText("Cardholder Signature: ___________________",  
				AutoText.POSITION_HEADER, 
				HorizontalBandAlignment.RIGHT);
		cardh.setWidth(800);
		cardh.setStyle(subheaderStyle.build());
		
		AutoText breakLine = new AutoText("",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.RIGHT);
		breakLine.setWidth(800);
		breakLine.setHeight(50);
		
		report.addAutoText(labelOwnerAddress);
		report.addAutoText(breakLine);
		report.addAutoText(terms);
		report.addAutoText(ownerNameLabel);
		report.addAutoText(oscas);
		report.addAutoText(tinNumberLabel);
		report.addAutoText(cardh);
		
	}


	private void setReportHeader(DynamicReportBuilder report) {
		
		StyleBuilder titleStyle = new StyleBuilder(true);
		titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
		
		StyleBuilder subheaderStyle = new StyleBuilder(true);
		subheaderStyle.setHorizontalAlign(HorizontalAlign.LEFT);
		
		AutoText punoName = new AutoText("PUNO'S FROZEN DAIRIES INC.",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		punoName.setStyle(titleStyle.build());
		punoName.setHeight(20);
		punoName.setWidth(350);
		
		AutoText salesInvoiceHeader = new AutoText("SALES INVOICE",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.RIGHT);
		salesInvoiceHeader.setStyle(titleStyle.build());
		salesInvoiceHeader.setHeight(20);
		salesInvoiceHeader.setWidth(200);
		
		AutoText subHeading1 = new AutoText("Manufacturer and Distributor of Puno’s Ice Cream and Sherbet Products",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		subHeading1.setWidth(350);
		subHeading1.setStyle(subheaderStyle.build());
		
		AutoText subHeadingRightInvoiceNum = new AutoText("NO. " + order.getInvoiceId(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.RIGHT);
		subHeadingRightInvoiceNum.setStyle(titleStyle.build());
		subHeadingRightInvoiceNum.setWidth(200);
		
		AutoText officeAddress = new AutoText("Office Address: Victoria Subd., Bitas, Cabanatuan City",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		officeAddress.setWidth(350);
		officeAddress.setStyle(subheaderStyle.build());
		
		AutoText salesOutlet = new AutoText("Sales Outlet: Mabini Street, Mabini Homesite, Cabanatuan City",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		salesOutlet.setWidth(350);
		salesOutlet.setStyle(subheaderStyle.build());
		
		AutoText orderDate = new AutoText(PfdiUtil.formatDate(order.getCreationDate()),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.RIGHT);
		orderDate.setStyle(titleStyle.build());
		orderDate.setHeight(20);
		orderDate.setWidth(350);
		
		AutoText vatRegistrationNumber = new AutoText("VAT Reg. TIN: 006-745-463-000",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		vatRegistrationNumber.setWidth(500);
		vatRegistrationNumber.setStyle(subheaderStyle.build());
		
		AutoText telNumber = new AutoText("Tel. No.: (044) 463-0818/464-8694",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		telNumber.setWidth(350);
		telNumber.setStyle(subheaderStyle.build());
		
		AutoText breakLine = new AutoText("",
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		breakLine.setWidth(350);
		breakLine.setHeight(10);
		

		
		
		AutoText owner = new AutoText("Store Address : " + order.getCustomer().getAddress(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		owner.setWidth(500);
		
		AutoText address = new AutoText("Owner Name : " + order.getCustomer().getOwnerName(),
				AutoText.POSITION_HEADER,
				HorizontalBandAlignment.LEFT);
		address.setWidth(500);	
		
		
		report.addAutoText(punoName);
		report.addAutoText(salesInvoiceHeader);
		report.addAutoText(subHeading1);
		report.addAutoText(subHeadingRightInvoiceNum);
		report.addAutoText(officeAddress);
		report.addAutoText(salesOutlet);
		report.addAutoText(orderDate);
		report.addAutoText(vatRegistrationNumber);
		report.addAutoText(telNumber);
		report.addAutoText(breakLine);
	
	}
	
	private void setReportFooter(DynamicReportBuilder report) {
		StyleBuilder subheaderStyle = new StyleBuilder(true);
		subheaderStyle.setHorizontalAlign(HorizontalAlign.LEFT);
		subheaderStyle.setVerticalAlign(VerticalAlign.TOP);
		
		Customer customer = order.getCustomer();
		
		AutoText labelOwnerAddress = new AutoText("Address: " + customer.getAddress(),
				AutoText.POSITION_FOOTER,
				HorizontalBandAlignment.LEFT);
		labelOwnerAddress.setWidth(250);
		labelOwnerAddress.setStyle(subheaderStyle.build());
		
		AutoText ownerNameLabel = new AutoText("Owner Name: " + customer.getOwnerName(),
				AutoText.POSITION_FOOTER,
				HorizontalBandAlignment.LEFT);
		ownerNameLabel.setWidth(250);
		ownerNameLabel.setStyle(subheaderStyle.build());
		
		AutoText tinNumberLabel = new AutoText("TIN Number: " + customer.getTinNumber(),
				AutoText.POSITION_FOOTER,
				HorizontalBandAlignment.LEFT);
		tinNumberLabel.setWidth(250);
		tinNumberLabel.setStyle(subheaderStyle.build());
		
		
		AutoText signatureLine = new AutoText("Signed By: _______________________________", 
				AutoText.POSITION_FOOTER, 
				HorizontalBandAlignment.RIGHT);
		signatureLine.setWidth(800);
		signatureLine.setHeight(30);
		signatureLine.setStyle(subheaderStyle.build());
		
//		AutoText singatureLabel = new AutoText("Authorized Signature            ", 
//				AutoText.POSITION_FOOTER, 
//				HorizontalBandAlignment.RIGHT);
//		singatureLabel.setWidth(800);
//		singatureLabel.setStyle(subheaderStyle.build());
		
		AutoText breakLine = new AutoText("",
				AutoText.POSITION_FOOTER,
				HorizontalBandAlignment.RIGHT);
		breakLine.setWidth(800);
		breakLine.setHeight(50);
		
		report.addAutoText(breakLine);
		report.addAutoText(signatureLine);
//		report.addAutoText(singatureLabel);
	}

}

package com.example.application.views.order;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableHead;
import org.vaadin.stefan.table.TableHeaderCell;
import org.vaadin.stefan.table.TableRow;

import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItemSalesInvoiceWrapper;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.reports.SalesInvoiceReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.MainLayout;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;

import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import net.sf.jasperreports.engine.JRException;

@PageTitle("Sales Invoice")
@Route(value = "order/salesInvoice/:id", layout = MainLayout.class)
@RouteAlias(value = "/order/salesInvoice/:id", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class SalesInvoiceView extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Button back;

	private OrdersService ordersService;

	private Order order;

	private String orderId;

	private Span salesInvoiceSpan;
	private Span orderDate;
	private Span storeName;
	private Span address;
	private Span ownerName;

	private byte[] salesInvoiceData;

	private SalesInvoiceReport salesInvoiceReport;
	private Div mainDiv = new Div();

//	private BigDecimal totalAmount = BigDecimal.valueOf(0);
//
//	private Span totalAmountLabel;
//	private AppUser appUser;
//
//	private DocumentTrackingNumberService documentTrackingNumberService;

	@Autowired
	public SalesInvoiceView(OrdersService ordersService, AuthenticatedUser user,
			DocumentTrackingNumberService documentTrackingNumberService, SalesInvoiceReport salesInvoiceReport) {
		this.ordersService = ordersService;
		this.salesInvoiceReport = salesInvoiceReport;
//		this.appUser = user.get().get();
//		this.documentTrackingNumberService = documentTrackingNumberService;
		addClassNames("administration-view");

		addChildrenToContentHeaderContainer(this);

		Div actionButtonDiv = new Div();
		actionButtonDiv.addClassName("action-button-wrapper");
		MenuBar menuBar = new MenuBar();
		menuBar.addClassName("float-right");
		menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

		actionButtonDiv.add(menuBar);
		add(actionButtonDiv);

		mainDiv.addClassName("order-summary-div");

//		createSummaryHeader(mainDiv);
//
//		createOrderDetailsDiv(mainDiv);
//
//		createFooterDiv(mainDiv);
//
		add(mainDiv);

		Div buttonContainer = new Div();
		buttonContainer.addClassName("order-summary-button-container");

		back = new Button("Back");
		back.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		back.setVisible(true);
		back.addClassNames("order-view-button", "float-right");
		back.addClickListener(e -> {
			UI.getCurrent().navigate(StockOrderView.class);
		});

		buttonContainer.add(back);

		add(buttonContainer);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		orderId = event.getRouteParameters().get("id").get();

		order = ordersService.get(Integer.parseInt(orderId)).get();

		
		Integer stockOrderNumber = order.getStockOrderNumber();
		if (stockOrderNumber == null) {
			stockOrderNumber = generateStockOrderNumber();
			order.setStockOrderNumber(stockOrderNumber);
		}

//		Integer salesInvoice = order.getInvoiceId();

//		salesInvoiceSpan.setText(salesInvoice.toString());
//		orderDate.setText(PfdiUtil.formatDateWithHours(order.getCreationDate()));
//		storeName.setText(order.getCustomer().getStoreName());
//		address.setText(order.getCustomer().getAddress());
//		ownerName.setText(order.getCustomer().getOwnerName());

		// set table summary content

		Map<String, List<OrderItems>> flavorsItemMap = order.getOrderItems().stream()
				.filter(e -> "Regular Ice Cream"
						.equalsIgnoreCase(e.getItemInventory().getProduct().getCategory().getCategoryName()))
				.collect(Collectors.groupingBy(
						orderItem -> orderItem.getItemInventory().getProduct().getCategory().getCategoryName()));

		Map<String, List<OrderItems>> regularFlavors = flavorsItemMap.get("Regular Ice Cream").stream()
				.collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getSize().getSizeName()));

		// Collect Regular Ice Cream Details
		List<OrderItemSalesInvoiceWrapper> orderItemSalesInvoiceWrapperList = Lists.newArrayList();
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

		List<OrderItems> otherFlavorsMapBySize = order.getOrderItems().stream().filter(e -> {
			return !"Regular Ice Cream"
					.equalsIgnoreCase(e.getItemInventory().getProduct().getCategory().getCategoryName());
		}).collect(Collectors.toList());

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

//		orderItemSalesInvoiceWrapperList.forEach((orderItemWrapper) -> {
//				
//				TableRow itemDetailsRow = table.getBody().addRow();
//				
//				TableDataCell quantityDataCell = itemDetailsRow.addDataCell();
//				quantityDataCell.setText(Integer.toString(orderItemWrapper.getQuantity()));
//				quantityDataCell.addClassNames("text-align-left");
//				
//				TableDataCell unitDataCell = itemDetailsRow.addDataCell();
//				unitDataCell.setText(orderItemWrapper.getUnit());
//				unitDataCell.addClassNames("text-align-left");
//				
//				TableDataCell particularsDataCell = itemDetailsRow.addDataCell();
//				particularsDataCell.setText(orderItemWrapper.getParticular());
//				particularsDataCell.addClassNames("text-align-left");
//				
//				TableDataCell unitPriceDataCell = itemDetailsRow.addDataCell();
//				unitPriceDataCell.setText(PfdiUtil.getFormatter().format(orderItemWrapper.getUnitPrice()));
//				unitPriceDataCell.addClassNames("text-align-right");
//				
//				TableDataCell amountDataCell = itemDetailsRow.addDataCell();
//				amountDataCell.setText(PfdiUtil.getFormatter().format(orderItemWrapper.getAmount()));
//				amountDataCell.addClassNames("text-align-right");
//
//		});

		// totalAmountLabel.setText("Total Amount : " +
		// PfdiUtil.getFormatter().format(totalAmount));
		try {
			salesInvoiceData = salesInvoiceReport.buildReport(order);
		} catch (ColumnBuilderException | ClassNotFoundException | JRException e1) {

			e1.printStackTrace();
		}
		PdfViewer pdfViewer = new PdfViewer();
		pdfViewer.setWidthFull();
		pdfViewer.setHeightFull();
		StreamResource resource = new StreamResource("order.pdf", () -> {
			if (order != null) {
				return new ByteArrayInputStream(salesInvoiceData);
			}
			return new ByteArrayInputStream(new byte[0]);
		});
		pdfViewer.setSrc(resource);
		mainDiv.add(pdfViewer);
		mainDiv.setWidthFull();
		mainDiv.setHeight("75%");
	}

	private void createSummaryHeader(Div mainDiv) {

		Div header = new Div();

		Div headerName = new Div();
		headerName.addClassNames("report-header-text-heading");
		Span punoName = new Span("PUNO'S FROZEN DAIRIES INC.");
		punoName.setWidthFull();

		Span salesInvoiceTitle = new Span("SALES INVOICE");

		salesInvoiceTitle.addClassName("float-right");
		headerName.add(punoName, salesInvoiceTitle);

		Div headerSub = new Div();
		Span punoSubHeader = new Span("Manufacturer and Distributor of Puno's Ice Cream & Sherbet Products");
		punoSubHeader.setWidthFull();

		Div invoiceNumDiv = new Div();

		invoiceNumDiv.addClassNames("bold-label", "report-header-text-heading", "float-right");
		Span salesInvoiceNoTitle = new Span("NO. ");
		salesInvoiceSpan = new Span();
		invoiceNumDiv.add(salesInvoiceNoTitle, salesInvoiceSpan);

		headerSub.add(punoSubHeader, invoiceNumDiv);

		Div headerSub2 = new Div();

		Span addressMainStore = new Span("Main Store: Mulawin 1, Bitas, Cabanatuan City");
		addressMainStore.setWidthFull();

		Div dateDiv = new Div();
		dateDiv.addClassNames("bold-label", "report-header-text-heading", "float-right");
		orderDate = new Span();
		orderDate.addClassName("float-right");
		orderDate.getStyle().set("margin-right", "-50px");
		dateDiv.add(orderDate);

		headerSub2.add(addressMainStore, dateDiv);

		Div line2 = new Div();
		Span salesOutlet = new Span("Sales Outlet: Victoria Subd., Bitas, Cabanatuan City");
		Span vatRegNumber = new Span("VAT Reg. TIN: 006-745-463-000");
		Span telNo = new Span("Tel. No.: (044) 463-0818/464-8694/330-3676");
		line2.add(new Html("<br>"), salesOutlet, new Html("<br>"), vatRegNumber, new Html("<br>"), telNo);

		Div ownerDetailsWrapper = new Div();
		ownerDetailsWrapper.addClassName("owner-details-wrapper");

		Div storeNameDiv = new Div();
		Span storeNameLabel = new Span("Store Name : ");
		storeName = new Span();
		storeName.addClassName("bold-label");

		Div termsDiv = new Div();
		termsDiv.setWidth("25%");
		termsDiv.addClassName("float-right");
		Span terms = new Span("Terms: ");
		// terms.addClassName("fillup-line");
		termsDiv.add(terms);

		header.addClassName("stock-order-number-date-container");

		storeNameDiv.add(storeNameLabel);
		storeNameDiv.add(storeName, termsDiv);

		Div addressDiv = new Div();
		Span addressLabel = new Span("Store Address : ");
		address = new Span();
		address.addClassName("bold-label");

		Div oscaDiv = new Div();
		oscaDiv.setWidth("25%");
		oscaDiv.addClassName("float-right");
		Span osca = new Span("OSCA/PWD ID No.: ");
		// osca.addClassName("fillup-line");
		oscaDiv.add(osca);

		addressDiv.add(addressLabel, address, oscaDiv);

		Div ownerNameDiv = new Div();
		Span ownerNameLabel = new Span("Owner Name : ");
		ownerName = new Span();
		ownerName.addClassName("bold-label");

		Div cardholderDiv = new Div();
		cardholderDiv.setWidth("25%");
		cardholderDiv.addClassNames("float-right");
		Span cardholder = new Span("Cardholder Signature.: ");
		cardholderDiv.add(cardholder);
		// cardholderDiv.addClassName("fillup-line");
		ownerNameDiv.add(ownerNameLabel, ownerName, cardholderDiv);

		header.add(headerName, headerSub, headerSub2, line2);

		ownerDetailsWrapper.add(storeNameDiv);
		ownerDetailsWrapper.add(addressDiv);
		ownerDetailsWrapper.add(ownerNameDiv);

		mainDiv.add(header);
		mainDiv.add(ownerDetailsWrapper);
		mainDiv.add(new Hr());
	}

	private void createFooterDiv(Div mainDiv) {
		Div disclaimerDiv = new Div();
		Span disclaimer1 = new Span("THIS SALES INVOICE SHALL BE VALID FOR FIVE (5) YEARS FROM THE DATE OF RELEASE..");
		disclaimerDiv.addClassNames("report-header-text-subheading", "padding-bottom-large");
		disclaimerDiv.add(disclaimer1);

		Div signatureLine = new Div();
		Span signatureText = new Span("Authorized Signature");
		signatureText.addClassNames("float-right", "text-align-center");
		signatureLine.addClassNames("report-header-text-subheading", "padding-bottom-large", "float-right",
				"signature-line");
		signatureLine.add(signatureText);

		mainDiv.add(disclaimerDiv, signatureLine);
	}

	private void createOrderDetailsDiv(Div mainDiv) {

//		table = new Table();
//		table.addClassName("order-delivery-receipt-table");
//
//		TableHead head = table.getHead();
//
//		TableRow headerRow = head.addRow();
//
//		TableHeaderCell quantityHeader = headerRow.addHeaderCell();
//		quantityHeader.setText("Quantity");
//		quantityHeader.addClassName("text-align-left");
//
//		TableHeaderCell unitHeader = headerRow.addHeaderCell();
//		unitHeader.setText("Unit");
//		unitHeader.addClassName("text-align-left");
//
//		TableHeaderCell particularHeader = headerRow.addHeaderCell();
//		particularHeader.setText("Particulars");
//		particularHeader.addClassName("text-align-left");
//
//		TableHeaderCell unitPriceHeader = headerRow.addHeaderCell();
//		unitPriceHeader.setText("Unit Price");
//		unitPriceHeader.addClassName("text-align-right");
//
//		TableHeaderCell amountHeader = headerRow.addHeaderCell();
//		amountHeader.setText("Amount");
//		amountHeader.addClassName("text-align-right");
//
//		mainDiv.add(new Hr());
//
//		storeName.addClassName("bold-label");
	}

	private Integer generateStockOrderNumber() {
		return ordersService.getLastId() + 1;
	}

	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		Div headerWrapper = new Div();
		headerWrapper.addClassName("order-summary-header-wrapper");

		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setHeightFull();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Create New Order > Summary");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper);
		headerWrapper.add(headerContainer);

		Hr hr = new Hr();

		Div hrWrapper = new Div();
		hrWrapper.addClassName("hr-div-class-wrapper");
		hrWrapper.add(hr);

		contentHeaderContainer.add(headerWrapper);
		contentHeaderContainer.add(hrWrapper);

	}

}

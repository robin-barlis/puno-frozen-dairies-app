package com.example.application.views.order;

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
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.MainLayout;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
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

@PageTitle("Stock Orders")
@Route(value = "order/stockTransfer/:id", layout = MainLayout.class)
@RouteAlias(value = "/order/stockTransfer/:id", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class StockTransferView extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Button back;

	private OrdersService ordersService;

	private Order order;

	private String orderId;

	private Span orderDate;
	private Span storeName;
	private Span stockOrderNum;
	private Span address;
	private Span ownerName;
	private Table table;


	@Autowired
	public StockTransferView(OrdersService ordersService, AuthenticatedUser user,
			DocumentTrackingNumberService documentTrackingNumberService) {
		this.ordersService = ordersService;
		addClassNames("administration-view");

		addChildrenToContentHeaderContainer(this);

		Div actionButtonDiv = new Div();
		actionButtonDiv.addClassName("action-button-wrapper");
		MenuBar menuBar = new MenuBar();
		menuBar.addClassName("float-right");
		menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

		actionButtonDiv.add(menuBar);
		add(actionButtonDiv);

		Div mainDiv = new Div();
		mainDiv.addClassName("order-summary-div");

		createSummaryHeader(mainDiv);

		createOrderDetailsDiv(mainDiv);

		createFooterDiv(mainDiv);

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

		
		orderDate.setText(PfdiUtil.formatDateWithHours(order.getCreationDate()));
		storeName.setText(order.getCustomer().getStoreName());
		address.setText(order.getCustomer().getAddress());
		ownerName.setText(order.getCustomer().getOwnerName());
		stockOrderNum.setText(order.getStockOrderNumber().toString());

		// set table summary content

		Map<String, List<OrderItems>> flavorsItemMap = order.getOrderItems().stream()
				.filter(e -> "Regular Ice Cream".equalsIgnoreCase(e.getItemInventory().getProduct().getCategory().getCategoryName()))
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

		orderItemSalesInvoiceWrapperList.forEach((orderItemWrapper) -> {
				
				TableRow itemDetailsRow = table.getBody().addRow();
				
				TableDataCell quantityDataCell = itemDetailsRow.addDataCell();
				quantityDataCell.setText(Integer.toString(orderItemWrapper.getQuantity()));
				quantityDataCell.addClassNames("text-align-left");
				
				TableDataCell unitDataCell = itemDetailsRow.addDataCell();
				unitDataCell.setText(orderItemWrapper.getUnit());
				unitDataCell.addClassNames("text-align-left");
				
				TableDataCell particularsDataCell = itemDetailsRow.addDataCell();
				particularsDataCell.setText(orderItemWrapper.getParticular());
				particularsDataCell.addClassNames("text-align-left");
//				
//				TableDataCell unitPriceDataCell = itemDetailsRow.addDataCell();
//				unitPriceDataCell.setText(PfdiUtil.getFormatter().format(orderItemWrapper.getUnitPrice()));
//				unitPriceDataCell.addClassNames("text-align-right");
//				
//				TableDataCell amountDataCell = itemDetailsRow.addDataCell();
//				amountDataCell.setText(PfdiUtil.getFormatter().format(orderItemWrapper.getAmount()));
//				amountDataCell.addClassNames("text-align-right");

		});

		// totalAmountLabel.setText("Total Amount : " +
		// PfdiUtil.getFormatter().format(totalAmount));
	}

	private void createSummaryHeader(Div mainDiv) {

		Div header = new Div();

		Div headerName = new Div();
		headerName.addClassNames("report-header-text-heading");
		Span punoName = new Span("PUNO'S FROZEN DAIRIES INC.");
		punoName.setWidthFull();

		Span salesInvoiceTitle = new Span("STOCK TRANSFER");

		salesInvoiceTitle.addClassName("float-right");
		headerName.add(punoName, salesInvoiceTitle);

		Div headerSub2 = new Div();

		Span addressMainStore = new Span("Victoria Subd., Bitas, Cabanatuan City");
		addressMainStore.setWidthFull();

		Div dateDiv = new Div();
		dateDiv.addClassNames("bold-label", "report-header-text-heading", "float-right");
		orderDate = new Span();
		orderDate.addClassName("float-right");
		orderDate.getStyle().set("margin-right", "-50px");
		dateDiv.add(orderDate);

		headerSub2.add(addressMainStore, dateDiv);

		Div line2 = new Div();
		Span salesOutlet = new Span("Victoria Plant");
		line2.add(new Html("<br>"), salesOutlet);


		Div ownerDetailsWrapper = new Div();
		ownerDetailsWrapper.addClassName("owner-details-wrapper");

		Div storeNameDiv = new Div();
		Span storeNameLabel = new Span("Store Name : ");
		storeName = new Span();
		storeName.addClassName("bold-label");
		

		header.addClassName("stock-order-number-date-container");

		storeNameDiv.add(storeNameLabel);
		storeNameDiv.add(storeName);

		Div stockOrderDiv = new Div();
		Span stockOrder = new Span("SO No. : ");
		stockOrderNum = new Span();
		stockOrderNum.addClassName("bold-label");
		
		
		stockOrderDiv.add(stockOrder);


		header.add(headerName, headerSub2, line2);

		ownerDetailsWrapper.add(storeNameDiv);
		ownerDetailsWrapper.add(stockOrderDiv);

		mainDiv.add(header);
		mainDiv.add(ownerDetailsWrapper);
		mainDiv.add(new Hr());
	}

	private void createFooterDiv(Div mainDiv) {
		Div disclaimerDiv = new Div();
		Span disclaimer1 = new Span("Received the above items in good order and condition.");
		disclaimerDiv.addClassNames("report-header-text-subheading", "padding-bottom-large");
		disclaimerDiv.add(disclaimer1);

		Div signatureLine = new Div();
		
		Span signatureText = new Span("Branch Personnel Signature Over Printed Name");

		signatureText.addClassNames("signature-line", "float-right", "text-align-center");
		signatureLine.addClassNames("report-header-text-subheading", "padding-bottom-large");
		signatureLine.add(signatureText);


		mainDiv.add(disclaimerDiv, signatureLine);
	}

	private void createOrderDetailsDiv(Div mainDiv) {

		table = new Table();
		table.addClassName("order-delivery-receipt-table");

		TableHead head = table.getHead();

		TableRow headerRow = head.addRow();

		TableHeaderCell quantityHeader = headerRow.addHeaderCell();
		quantityHeader.setText("Quantity");
		quantityHeader.addClassName("text-align-left");

		TableHeaderCell unitHeader = headerRow.addHeaderCell();
		unitHeader.setText("Size");
		unitHeader.addClassName("text-align-left");

		TableHeaderCell particularHeader = headerRow.addHeaderCell();
		particularHeader.setText("Product");
		particularHeader.addClassName("text-align-left");

		mainDiv.add(table);
		mainDiv.add(new Hr());

		storeName.addClassName("bold-label");
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

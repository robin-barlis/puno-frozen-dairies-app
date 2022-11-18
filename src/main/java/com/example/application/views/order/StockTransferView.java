package com.example.application.views.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableHead;
import org.vaadin.stefan.table.TableHeaderCell;
import org.vaadin.stefan.table.TableRow;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.MainLayout;
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
@RolesAllowed({"Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class StockTransferView extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;
	
	private Button back;

	private OrdersService ordersService;
	
	private Order order;
	
	private String orderId;
	
	private Span stockOrderNumberSpan;
	private Span deliveryReceiptNumberSpan;
	private Span orderDate;
	private Span storeName;
	private Span address;	
	private Span ownerName;
	private Table table;

	private BigDecimal totalAmount = BigDecimal.valueOf(0);

	private Span totalAmountLabel;
	private AppUser appUser;

	private DocumentTrackingNumberService documentTrackingNumberService;


	@Autowired
	public StockTransferView(OrdersService ordersService, AuthenticatedUser user, DocumentTrackingNumberService documentTrackingNumberService) {
		this.ordersService = ordersService;
		this.appUser = user.get().get();
		this.documentTrackingNumberService = documentTrackingNumberService;
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
		
		Integer deliveryReceipt = order.getDeliveryReceiptId();

		stockOrderNumberSpan.setText(stockOrderNumber.toString());
		deliveryReceiptNumberSpan.setText(deliveryReceipt.toString());
		orderDate.setText(PfdiUtil.formatDateWithHours(order.getCreationDate()));
		storeName.setText(order.getCustomer().getStoreName());
		address.setText(order.getCustomer().getAddress());
		ownerName.setText(order.getCustomer().getOwnerName());
		
		//set table summary content
		
		Map<String, List<OrderItems>> orderItems = order.getOrderItems().stream().collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getProductName()));;
		
		
		orderItems.forEach((itemName, sizeDetails) -> {
			TableRow detailsRowFlavor = table.getBody().addRow();
			TableDataCell dataCell = detailsRowFlavor.addDataCell();
			dataCell.setText(itemName);
			dataCell.setColSpan(3);
			
			sizeDetails.forEach(item -> {
				String sizeName = item.getItemInventory().getSize().getSizeName();
				Integer quantity = item.getQuantity();
				
				BigDecimal unitPrice = item.getProductPrice();
				BigDecimal amount = item.getProductPrice().multiply(BigDecimal.valueOf(quantity));
				totalAmount = totalAmount.add(amount);
				
				TableRow itemDetailsRow = table.getBody().addRow();
				TableDataCell sizeDataCell = itemDetailsRow.addDataCell();
				sizeDataCell.setText(sizeName);
				sizeDataCell.addClassNames("text-align-left", "padding-left-50px");
				
				TableDataCell unitPriceDataCell = itemDetailsRow.addDataCell();
				unitPriceDataCell.setText(PfdiUtil.getFormatter().format(unitPrice));
				unitPriceDataCell.addClassName("text-align-right");
				
				
				TableDataCell quantityDataCell = itemDetailsRow.addDataCell();
				quantityDataCell.setText(quantity.toString());
				quantityDataCell.addClassName("text-align-right");
				

			});
		});
		
		//totalAmountLabel.setText("Total Amount : " + PfdiUtil.getFormatter().format(totalAmount));
	}
	
	

	private void createSummaryHeader(Div mainDiv) {
		
		Div header = new Div();	
		
		Div headerName = new Div();
		headerName.addClassNames("text-align-center", "report-header-text-heading");
		Span punoName = new Span("Puno's Frozen Dairies INC.");
		punoName.setWidthFull();
		headerName.add(punoName);
		
		Div headerSub = new Div();
		headerSub.addClassNames("text-align-center", "report-header-text-subheading");
		Span punoSubHeader = new Span("Manufacturer & Distributor of Puno's Ice Cream & Sherbet Products");
		punoSubHeader.setWidthFull();
		headerSub.add(punoSubHeader);
		
		
		Div line1 = new Div();
		line1.addClassNames("report-header-text-subheading");
		Span punoAddress = new Span("Address: Victoria Subd., Bitas, Cabanatuan City");
		Span telNo = new Span("Tel. No.: (044) 463-0818/464-8694/330-3676");
		telNo.addClassName("float-right");
		line1.add(punoAddress, telNo);
		
		
		Div line2 = new Div();

		line2.addClassNames("report-header-text-subheading");
		Span addressMainStore = new Span("Main Store: Mulawin 1, Bitas, Cabanatuan City");
		Span mobileNumber = new Span("Mobile No.: 0932-881-4249/0922-533-4987");
		mobileNumber.addClassName("float-right");
		line2.add(addressMainStore, mobileNumber);
		
		Div line3 = new Div();
		line3.addClassNames("text-align-center","report-header-text-subheading");
		Span vatRegNumber = new Span("VAT Reg. TIN: 006-745-463-000");
		line3.add(vatRegNumber);
		
		
		
//		headerName.add(stockOrderNumberSpam);
//		headerName.add(orderDate);
//		orderDate.addClassName("float-right");
		
		Div deliveryReceiptDiv = new Div();

		Span deliveryReceiptTitle = new Span("Delivery Receipt No. ");
		deliveryReceiptTitle.addClassName("bold-label");
		deliveryReceiptNumberSpan = new Span();
		

		Div stockOrderDiv = new Div();
		stockOrderDiv.addClassName("padding-bottom-medium");
		Span stockOrderTitle = new Span("S.O. No. ");
		stockOrderNumberSpan = new Span();
		stockOrderDiv.add(stockOrderTitle, stockOrderNumberSpan);
		
		orderDate = new Span();
		deliveryReceiptDiv.add(deliveryReceiptTitle, deliveryReceiptNumberSpan);
		deliveryReceiptDiv.add(orderDate);
		orderDate.addClassName("float-right");
		
		Div ownerDetailsWrapper = new Div();
		ownerDetailsWrapper.addClassName("owner-details-wrapper");
		
		Div storeNameDiv = new Div();
		Span storeNameLabel = new Span("Store Name : ");
		storeName = new Span();
		storeName.addClassName("bold-label");
		
		
		header.addClassName("stock-order-number-date-container");
		
		storeNameDiv.add(storeNameLabel);
		storeNameDiv.add(storeName);
		
		Div addressDiv = new Div();
		Span addressLabel = new Span("Store Address : ");
		address = new Span();
		address.addClassName("bold-label");
		addressDiv.add(addressLabel);
		addressDiv.add(address);
		
		Div ownerNameDiv = new Div();
		Span ownerNameLabel = new Span("Owner Name : ");
		ownerName = new Span();
		ownerName.addClassName("bold-label");
		ownerNameDiv.add(ownerNameLabel);
		ownerNameDiv.add(ownerName);
		
		
		header.add(headerName, headerSub, line1, line2, line3);
		
		ownerDetailsWrapper.add(deliveryReceiptDiv);
		ownerDetailsWrapper.add(stockOrderDiv);
		ownerDetailsWrapper.add(storeNameDiv);
		ownerDetailsWrapper.add(addressDiv);
		ownerDetailsWrapper.add(ownerNameDiv);
		
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
		Span signatureText = new Span("Customer Signature Over Printed Name");
		signatureText.addClassNames("signature-line","float-right");
		signatureLine.addClassNames("report-header-text-subheading", "padding-bottom-large");
		signatureLine.add(signatureText);
		
		
		Div disclaimerDiv2 = new Div();
		Span disclaimer2 = new Span("\"THIS DOCUMENT IS NOT VALID FOR CLAIM OF INPUT TAXES.\"");
		disclaimerDiv2.addClassNames("report-header-text-subheading", "text-align-center");
		disclaimerDiv2.add(disclaimer2);
		
		Div disclaimerDiv3 = new Div();
		Span disclaimer3 = new Span("THIS DELIVERY RECEIPT SHALL BE VALID FOR FIVE (5) YEARS FROM THE DATE OF RELEASE.");
		disclaimerDiv3.addClassNames("report-header-text-subheading", "text-align-center");
		disclaimerDiv3.add(disclaimer3);
		
		
		mainDiv.add(disclaimerDiv, signatureLine, disclaimerDiv2, disclaimerDiv3);
	}

	private void createOrderDetailsDiv(Div mainDiv) {
		
		table = new Table();
		table.addClassName("order-delivery-receipt-table");

		TableHead head = table.getHead();
		
		TableRow headerRow = head.addRow();
		
		
		TableHeaderCell itemHeaderCell =  headerRow.addHeaderCell();
		itemHeaderCell.setText("Item");
		itemHeaderCell.addClassName("text-align-left");
		
		TableHeaderCell unitPriceHeaderCell = headerRow.addHeaderCell();
		unitPriceHeaderCell.setText("Unit Price");
		unitPriceHeaderCell.addClassName("text-align-right");
		
		TableHeaderCell quantityHeaderCell = headerRow.addHeaderCell();
		quantityHeaderCell.setText("Quantity");
		quantityHeaderCell.addClassName("text-align-right");
		
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

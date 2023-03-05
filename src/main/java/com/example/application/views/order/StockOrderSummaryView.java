package com.example.application.views.order;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.vaadin.stefan.table.Table;

import com.example.application.data.DocumentTrackingNumberEnum;
import com.example.application.data.OrderStatus;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.DocumentTrackingNumber;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.SizesService;
import com.example.application.reports.OrderSummaryReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.MainLayout;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrderSummary/:id", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrderSummary/:id", layout = MainLayout.class)
@RolesAllowed({"Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class StockOrderSummaryView extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Button saveAsDraft;
	
	private Button createSalesInvoice;
	
	private Button createStockTransfer;
	
	private Button submit;
	
	private Button back;

	private OrdersService ordersService;
	
	private Order order;
	
	private String orderId;
	private AppUser appUser;
	private MenuItem editMenu;
	//private MenuItem readyForDelivery;
	private MenuItem delivered;
	private SizesService sizesService;
	private OrderSummaryReport orderSummaryReport;

	private byte[] orderSummaryReportData;
	private Div mainDiv = new Div();


	@Autowired
	public StockOrderSummaryView(SizesService sizesService, 
			OrdersService ordersService, 
			AuthenticatedUser user, 
			OrderSummaryReport orderSummaryReport, 
			DocumentTrackingNumberService documentTrackingNumberService) {
		this.ordersService = ordersService;
		this.appUser = user.get().get();
		
		this.sizesService = sizesService;
		this.orderSummaryReport = orderSummaryReport;
		addClassNames("administration-view");
		
		addChildrenToContentHeaderContainer(this);
		
		Div actionButtonDiv = new Div();
		actionButtonDiv.addClassName("action-button-wrapper");
		MenuBar menuBar = new MenuBar();
		menuBar.addClassName("float-right");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        editMenu = createIconItem(menuBar, VaadinIcon.EDIT, "Edit Stock Order");
        editMenu.addClickListener(e -> {
        	RouteParam param = new RouteParam("orderId", orderId);
        	RouteParameters routeParams = new RouteParameters(param);
        	UI.getCurrent().navigate(CreateOrderFormView.class, routeParams);
        });
       
        delivered = createIconItem(menuBar, VaadinIcon.FLAG_CHECKERED, "Set order to delivered");
        delivered.addClickListener(e -> {
        	ConfirmDialog confirmDialog = new ConfirmDialog();
      	
        	confirmDialog.setCancelable(true);
        	confirmDialog.setHeader("Are you sure that this order has already been delivered?");
        	
        	Button confirmButton = new Button("Confirm");
        	confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);       	

        	
         	confirmButton.addClickListener(buttonClickListener -> {
				setStatus(OrderStatus.DELIVERED);

				Notification.show("Stock Order #" + order.getStockOrderNumber() + " has been delivered.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(StockOrderView.class);
				confirmDialog.close();
        	
        	});
         	
         	confirmDialog.setConfirmButton(confirmButton);
        	confirmDialog.open();
        });
        
        
        actionButtonDiv.add(menuBar);
		add(actionButtonDiv);
        

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
		
		saveAsDraft = new Button("Save As Draft");
		saveAsDraft.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		saveAsDraft.setVisible(false);
		saveAsDraft.addClassNames("order-view-button", "float-right");
		saveAsDraft.addClickListener(e -> {
			Notification.show("Order saved as draft.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			UI.getCurrent().navigate(StockOrderView.class);
		});
		
				
		submit = new Button("Submit");
		submit.setVisible(false);
		submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		submit.addClassNames("order-view-button", "float-right");
		submit.addClickListener(e -> {
			order.setStatus(OrderStatus.FOR_DELIVERY.getOrderStatusName());
			order = ordersService.update(order);
			Notification.show("Stock Order #" + order.getStockOrderNumber() + " successfully created.");
			UI.getCurrent().navigate(StockOrderView.class);
		});
		
		createSalesInvoice = new Button("Create S.I. & D.R.");
		createSalesInvoice.setTooltipText("Create Sales Invoice and Delivery Receipt");
		createSalesInvoice.setVisible(false);
		createSalesInvoice.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createSalesInvoice.addClassNames("order-view-button", "float-right");
		createSalesInvoice.addClickListener(e -> {
			
			DocumentTrackingNumber invoiceNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.INVOICE_NUMBER.name());
			DocumentTrackingNumber deliveryReceiptNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.DELIVERY_RECEIPT_NUMBER.name());
			DocumentTrackingNumber stockTransferNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.STOCK_TRANSFER_NUMBER.name());
			
			
			Integer currentInvoiceNumber = 0;
			if (invoiceNumber == null) {
				invoiceNumber = new DocumentTrackingNumber();
				invoiceNumber.setType(DocumentTrackingNumberEnum.INVOICE_NUMBER.name());
			} else {
				currentInvoiceNumber = invoiceNumber.getNumber();
			}
			
			invoiceNumber.setNumber(currentInvoiceNumber + 1);
			
					
			Integer currentDeliveryReceiptNumber = 0;
			if (deliveryReceiptNumber == null) {
				deliveryReceiptNumber = new DocumentTrackingNumber();
				deliveryReceiptNumber.setType(DocumentTrackingNumberEnum.DELIVERY_RECEIPT_NUMBER.name());
			} else {
				currentDeliveryReceiptNumber = deliveryReceiptNumber.getNumber();
			}
			
			deliveryReceiptNumber.setNumber(currentDeliveryReceiptNumber + 1);
			
			
			Integer currentStockTransferNumber = 0;
			if (stockTransferNumber == null) {
				stockTransferNumber = new DocumentTrackingNumber();
				stockTransferNumber.setType(DocumentTrackingNumberEnum.STOCK_TRANSFER_NUMBER.name());
			} else {
				currentStockTransferNumber = deliveryReceiptNumber.getNumber();
			}
			stockTransferNumber.setNumber(currentStockTransferNumber + 1);
			
			stockTransferNumber = documentTrackingNumberService.update(stockTransferNumber);
			invoiceNumber = documentTrackingNumberService.update(invoiceNumber);
			deliveryReceiptNumber = documentTrackingNumberService.update(deliveryReceiptNumber);
			
			order.setStatus(OrderStatus.FOR_DELIVERY.getOrderStatusName());
			order.setDeliveryReceiptId(deliveryReceiptNumber.getNumber());
			order.setInvoiceId(invoiceNumber.getNumber());
			order.setStockTransferId(stockTransferNumber.getNumber());
			
			order = ordersService.update(order);
			Notification.show("Delivery Receipt, Stock Trasnfer, and Invoice numbers for" + order.getStockOrderNumber() + " successfully created.");
			RouteParameters parameters = new RouteParameters("id", order.getId().toString());
			UI.getCurrent().navigate(DeliveryReceiptView.class, parameters);
		});
		
		createStockTransfer = new Button("Create S.T. & D.R.");
		createStockTransfer.setTooltipText("Create Stock Transfer and Delivery Receipt");
		createStockTransfer.setVisible(false);
		createStockTransfer.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createStockTransfer.addClassNames("order-view-button", "float-right");
		createStockTransfer.addClickListener(e -> {
			DocumentTrackingNumber stockTransferNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.STOCK_TRANSFER_NUMBER.name());
			DocumentTrackingNumber deliveryReceiptNumber = documentTrackingNumberService.findByType(DocumentTrackingNumberEnum.DELIVERY_RECEIPT_NUMBER.name());
			
			Integer currentInvoiceNumber = stockTransferNumber.getNumber()+1;
			stockTransferNumber.setNumber(currentInvoiceNumber);
			
			Integer currentDeliveryReceiptNumber = deliveryReceiptNumber.getNumber()+1;
			deliveryReceiptNumber.setNumber(currentDeliveryReceiptNumber);
			
			stockTransferNumber = documentTrackingNumberService.update(stockTransferNumber);
			deliveryReceiptNumber = documentTrackingNumberService.update(deliveryReceiptNumber);
			
			order.setStatus(OrderStatus.FOR_DELIVERY.getOrderStatusName());
			order.setDeliveryReceiptId(deliveryReceiptNumber.getNumber());
			order.setStockTransferId(stockTransferNumber.getNumber());
			
			order = ordersService.update(order);
			Notification.show("Delivery Receipt & Invoice numbers for" + order.getStockOrderNumber() + " successfully created.");
			UI.getCurrent().navigate(StockOrderView.class);
		});
		
		buttonContainer.add(submit, saveAsDraft, createSalesInvoice, createStockTransfer, back );
		
		add(buttonContainer);
	}

	private Order setStatus(OrderStatus forDelivery) {
		order.setStatus(forDelivery.getOrderStatusName());
		order.setCheckedByUser(appUser);
		order.setCheckedDate(LocalDateTime.now());
		order.setUpdatedByUser(appUser);
		order.setUpdatedDate(LocalDateTime.now());
		
		return ordersService.update(order);
	}

	private MenuItem createIconItem(MenuBar menu, VaadinIcon iconName, String tooltipText) {
		Icon icon = new Icon(iconName);
        MenuItem item = menu.addItem(icon, tooltipText);
        return item;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		orderId = event.getRouteParameters().get("id").get();
		
		order = ordersService.get(Integer.parseInt(orderId)).get();

	//	try {
			orderSummaryReportData = orderSummaryReport.buildReport(order, order.getOrderItems(), sizesService.listAll(Sort.unsorted()), appUser);
//		} catch (ColumnBuilderException | ClassNotFoundException | JRException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Integer stockOrderNumber = order.getStockOrderNumber();
		if (stockOrderNumber == null) {
			stockOrderNumber = generateStockOrderNumber();
			order.setStockOrderNumber(stockOrderNumber);
		}
		
		PdfViewer pdfViewer = new PdfViewer();
		pdfViewer.setWidthFull();
		pdfViewer.setHeightFull();
		StreamResource resource = new StreamResource("order.pdf", () -> {
			if (order != null) {
				return new ByteArrayInputStream(orderSummaryReportData);
			}
			return new ByteArrayInputStream(new byte[0]);
		});
		pdfViewer.setAddDownloadButton(true);
		pdfViewer.setAddPrintButton(true);
		pdfViewer.setSrc(resource);
		mainDiv.add(pdfViewer);
		mainDiv.setWidthFull();
		mainDiv.setHeight("75%");
		
		
		//ONLY VISIBLE WHEN DRAFT, FOR EDITING
		submit.setVisible((PfdiUtil.isOrderStatusEquals(order.getStatus(), OrderStatus.DRAFT))
				&& (PfdiUtil.isSales(appUser) || PfdiUtil.isSuperUser(appUser)));
		saveAsDraft.setVisible((PfdiUtil.isOrderStatusEquals(order.getStatus(), OrderStatus.DRAFT))
				&& (PfdiUtil.isSales(appUser) || PfdiUtil.isSuperUser(appUser)));
		
		createStockTransfer.setVisible(order.getStockTransferId() == null && 
				(PfdiUtil.isChecker(appUser) || PfdiUtil.isSuperUser(appUser))
				&& PfdiUtil.isRelativeOrCompanyOwned(order.getCustomer().getCustomerTagId()));
		createSalesInvoice.setVisible(order.getInvoiceId() == null &&
				(PfdiUtil.isChecker(appUser) || PfdiUtil.isSuperUser(appUser))
				&& !PfdiUtil.isRelativeOrCompanyOwned(order.getCustomer().getCustomerTagId()));

		
		editMenu.setVisible(PfdiUtil.isOrderStatusEquals(order.getStatus(), OrderStatus.DRAFT)
				&& (PfdiUtil.isSales(appUser) || PfdiUtil.isSuperUser(appUser)));
		
		delivered.setVisible((PfdiUtil.isOrderStatusEquals(order.getStatus(), OrderStatus.FOR_DELIVERY))
				&& (PfdiUtil.isChecker(appUser) || PfdiUtil.isSuperUser(appUser)));
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

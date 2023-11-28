package com.example.application.views.order;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.DocumentTrackingNumberEnum;
import com.example.application.data.OrderStatus;
import com.example.application.data.Reports;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.DocumentTrackingNumber;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.CategoryService;
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
import com.vaadin.flow.spring.annotation.UIScope;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrderSummary/:id", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrderSummary/:id", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
@UIScope
public class StockOrderSummaryView extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;
	private Button generateReceipts;
	private Button back;
	private OrdersService ordersService;
	private Order order;
	private String orderId;
	private AppUser appUser;
	private MenuItem editMenu;
	private MenuItem delivered;
	private SizesService sizesService;
	private OrderSummaryReport orderSummaryReport;
	private CategoryService categoryService;
	private byte[] orderSummaryReportData;
	private Div mainDiv = new Div();

	@Autowired
	public StockOrderSummaryView(SizesService sizesService, OrdersService ordersService, AuthenticatedUser user,
			OrderSummaryReport orderSummaryReport, DocumentTrackingNumberService documentTrackingNumberService,
			CategoryService categoryService) {
		this.ordersService = ordersService;
		this.appUser = user.get().get();
		this.categoryService = categoryService;

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

				Notification.show("Stock Order #" + order.getStockOrderNumber() + " has been delivered.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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

		back = new Button("Cancel");
		back.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		back.setVisible(true);
		back.addClassNames("order-view-button", "float-right");
		back.addClickListener(e -> {
			UI.getCurrent().navigate(StockOrderView.class);
		});

		generateReceipts = new Button();
		generateReceipts.setTooltipText("Generate Receipts");
		generateReceipts.setVisible(false);
		generateReceipts.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		generateReceipts.addClassNames("order-view-button", "float-right");

		generateReceipts.setText("Generate Receipts");
		generateReceipts.addClickListener(e -> {

			if (!isReceiptsGenerated(order)) {
				setInvoiceNumber(documentTrackingNumberService);
				updateDeliveryReceiptNumber(documentTrackingNumberService);
				setStockTransferNumber(documentTrackingNumberService);
				order.setStatus(OrderStatus.FOR_DELIVERY.getOrderStatusName());
				order = ordersService.update(order);
				Notification.show("Delivery Receipt, Stock Transfer, and Invoice numbers for "
						+ order.getStockOrderNumber() + " successfully created.");
			}
			RouteParam idParam = new RouteParam("id", String.join(",", order.getId().toString()));

			RouteParam report = new RouteParam("report", Reports.ALL.name());
			RouteParameters parameters = new RouteParameters(idParam, report);

			UI.getCurrent().navigate(PrinterView.class, parameters);
		});

		buttonContainer.add(generateReceipts, back);
		add(buttonContainer);
	}

	private void setStockTransferNumber(DocumentTrackingNumberService documentTrackingNumberService) {

		if (order.getStockTransferId() == null) {
			DocumentTrackingNumber stockTransferNumber = documentTrackingNumberService
					.findByType(DocumentTrackingNumberEnum.STOCK_TRANSFER_NUMBER.name());
			Integer currentStockTransferNumber = 0;
			if (stockTransferNumber == null) {
				stockTransferNumber = new DocumentTrackingNumber();
				stockTransferNumber.setType(DocumentTrackingNumberEnum.STOCK_TRANSFER_NUMBER.name());
			} else {
				currentStockTransferNumber = stockTransferNumber.getNumber();
			}
			stockTransferNumber.setNumber(currentStockTransferNumber + 1);
			stockTransferNumber = documentTrackingNumberService.update(stockTransferNumber);
			order.setStockTransferId(stockTransferNumber.getNumber());
		}
	}

	private void updateDeliveryReceiptNumber(DocumentTrackingNumberService documentTrackingNumberService) {
		if (order.getDeliveryReceiptId() == null) {
			DocumentTrackingNumber deliveryReceiptNumber = documentTrackingNumberService
					.findByType(DocumentTrackingNumberEnum.DELIVERY_RECEIPT_NUMBER.name());
			Integer currentDeliveryReceiptNumber = 0;
			if (deliveryReceiptNumber == null) {
				deliveryReceiptNumber = new DocumentTrackingNumber();
				deliveryReceiptNumber.setType(DocumentTrackingNumberEnum.DELIVERY_RECEIPT_NUMBER.name());
			} else {
				currentDeliveryReceiptNumber = deliveryReceiptNumber.getNumber();
			}
			deliveryReceiptNumber.setNumber(currentDeliveryReceiptNumber + 1);
			deliveryReceiptNumber = documentTrackingNumberService.update(deliveryReceiptNumber);
			order.setDeliveryReceiptId(deliveryReceiptNumber.getNumber());

		}

	}

	private void setInvoiceNumber(DocumentTrackingNumberService documentTrackingNumberService) {

		if (!PfdiUtil.isCompanyOwned(order.getCustomer().getCustomerTagId())) {

			if (order.getInvoiceId() == null) {
				DocumentTrackingNumber invoiceNumber = documentTrackingNumberService
						.findByType(DocumentTrackingNumberEnum.INVOICE_NUMBER.name());
				Integer currentInvoiceNumber = 0;
				if (invoiceNumber == null) {
					invoiceNumber = new DocumentTrackingNumber();
					invoiceNumber.setType(DocumentTrackingNumberEnum.INVOICE_NUMBER.name());
				} else {
					currentInvoiceNumber = invoiceNumber.getNumber();
				}

				invoiceNumber.setNumber(currentInvoiceNumber + 1);
				invoiceNumber = documentTrackingNumberService.update(invoiceNumber);
				order.setInvoiceId(invoiceNumber.getNumber());
			}
		}
	}

	private Order setStatus(OrderStatus forDelivery) {
		order.setStatus(forDelivery.getOrderStatusName());
		order.setDeliveryDate(LocalDateTime.now());
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

		orderSummaryReportData = orderSummaryReport.buildReport(order, order.getOrderItems(),
				sizesService.listAll(Sort.unsorted()), appUser, categoryService.listAll(Sort.unsorted()));

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

		boolean isReceiptGenerated = isReceiptsGenerated(order);
		String receiptsLabel = isReceiptGenerated ? "View Receipts" : "Generate Receipts";
		generateReceipts.setVisible(true);
		generateReceipts.setText(receiptsLabel);
		editMenu.setVisible(PfdiUtil.isOrderStatusEquals(order.getStatus(), OrderStatus.DRAFT)
				&& (PfdiUtil.isSales(appUser) || PfdiUtil.isSuperUser(appUser)));
		delivered.setVisible((PfdiUtil.isOrderStatusEquals(order.getStatus(), OrderStatus.FOR_DELIVERY))
				&& (PfdiUtil.isChecker(appUser) || PfdiUtil.isSuperUser(appUser)));
	}

	private boolean isReceiptsGenerated(Order order) {

		if (order == null) {
			return false;
		}

		Integer stockOrderNo = order.getStockOrderNumber();
		Integer stockTransfer = order.getStockTransferId();
		Integer deliveryReceiptNo = order.getDeliveryReceiptId();
		Integer invoiceNo = order.getInvoiceId();

		if (PfdiUtil.isCompanyOwned(order.getCustomer().getCustomerTagId())) {
			return stockOrderNo != null && stockTransfer != null;
		} else {
			return stockOrderNo != null && stockTransfer != null && deliveryReceiptNo != null && invoiceNo != null;
		}

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

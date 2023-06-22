
package com.example.application.views.order;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.vaadin.klaudeta.PaginatedGrid;

import com.example.application.data.OrderStatus;
import com.example.application.data.PaymentStatus;
import com.example.application.data.Reports;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrderRepositoryCustom;
import com.example.application.data.service.orders.OrderRepositoryCustomImpl;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.SizesService;
import com.example.application.reports.OrderSummaryReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.utils.service.ReportConsolidatorService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.order.offerings.OfferingsView;
import com.google.common.collect.Maps;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrders", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrders", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Checker", "CHECKER" })
@Uses(Icon.class)
public class StockOrderView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	PaginatedGrid<Order, ?> grid = new PaginatedGrid<>();
	private Button addStockOrderButton;

	private OrdersService ordersService;
	private final CustomerService customerService;

	private ListDataProvider<Order> ldp = null;
	List<Order> orders;
	List<Customer> customers;
	private Dialog searchOrdersDialog = new Dialog();
	private final OrderRepositoryCustom orderRepositoryCustom;
	private AppUser appUser;

	@Autowired
	public StockOrderView(OrdersService ordersService, CustomerService customerService, AuthenticatedUser user,
			OrderSummaryReport orderSummaryReport, SizesService sizesService,
			OrderRepositoryCustomImpl orderRepositoryCustom, ReportConsolidatorService reportConsolidatorService) {
		super("Admin", "Admin");
		this.customerService = customerService;
		this.ordersService = ordersService;
		this.orderRepositoryCustom = orderRepositoryCustom;
		this.appUser = user.get().get();
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);
		createSearchDialog("Search Orders");

		add(tableContent);

	}

	private void createSearchDialog(String label) {
		Label addProfileLabel = new Label(label);
		addProfileLabel.getStyle().set("padding-bottom", "20px");

		TextField orderId = new TextField("Stock Order Number");

		TextField deliveryReceiptField = new TextField("Delivery Receipt Number");

		TextField stockTransferField = new TextField("Stock Transfer Number");

		TextField invoiceNUmberField = new TextField("Invoice Number");

		List<Customer> roles = customerService.listAll(Sort.unsorted());

		MultiSelectComboBox<Customer> storeName = new MultiSelectComboBox<>();
		storeName.setLabel("Store Name");
		storeName.setItems(roles);
		storeName.setPlaceholder("Select Store Name");
		storeName.setItemLabelGenerator(e -> e.getStoreName());

		MultiSelectComboBox<PaymentStatus> paymentStatus = new MultiSelectComboBox<>();

		List<PaymentStatus> paymentStatuses = Arrays.asList(PaymentStatus.values());
		paymentStatus.setLabel("Payment Status");
		paymentStatus.setItems(paymentStatuses);
		paymentStatus.setPlaceholder("Select Payment Status");
		paymentStatus.setItemLabelGenerator(e -> e.getPaymentStatusName());

		MultiSelectComboBox<OrderStatus> orderStatusField = new MultiSelectComboBox<>();

		List<OrderStatus> orderStatus = Arrays.asList(OrderStatus.values());
		orderStatusField.setLabel("Order Status");
		orderStatusField.setItems(orderStatus);
		orderStatusField.setPlaceholder("Select Order Status");
		orderStatusField.setItemLabelGenerator(e -> e.getOrderStatusName());

		DatePicker orderDateTo = new DatePicker("Order Date To");
		orderDateTo.setEnabled(false);

		DatePicker orderDateFrom = new DatePicker("Order Date From");
		orderDateFrom.addValueChangeListener(e -> {
			orderDateTo.setValue(LocalDate.now());
			orderDateTo.setEnabled(true);
		});

		DatePicker dueDateTo = new DatePicker("Due Date To");
		dueDateTo.getStyle().set("padding-bottom", "40px");
		dueDateTo.setEnabled(false);

		DatePicker dueDateFrom = new DatePicker("Due Date From");
		dueDateFrom.getStyle().set("padding-bottom", "40px");
		dueDateFrom.addValueChangeListener(e -> {
			dueDateTo.setValue(LocalDate.now());
			dueDateTo.setEnabled(true);
		});

		Button searchButton = new Button("Search");
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.addClickListener(e -> {

			Map<String, Object> filters = Maps.newHashMap();

			if (!orderId.isEmpty()) {
				filters.put("stockOrderNumber", orderId.getValue());
			}

			if (!deliveryReceiptField.isEmpty()) {
				filters.put("deliveryReceiptId", deliveryReceiptField.getValue());
			}

			if (!stockTransferField.isEmpty()) {
				filters.put("stockTransferId", stockTransferField.getValue());
			}

			if (!invoiceNUmberField.isEmpty()) {
				filters.put("invoiceId", invoiceNUmberField.getValue());
			}

			if (!storeName.isEmpty()) {
				filters.put("store", storeName.getValue());
			}

			if (!orderStatusField.isEmpty()) {

				List<String> orderStatusName = orderStatusField.getValue().stream().map(osf -> osf.getOrderStatusName())
						.collect(Collectors.toList());
				filters.put("orderStatus", orderStatusName);
			}

			if (!orderDateFrom.isEmpty()) {

				Map<String, LocalDate> orderDates = Maps.newHashMap();
				orderDates.put("orderDateFrom", orderDateFrom.getValue());
				orderDates.put("orderDateTo", !orderDateTo.isEmpty() ? orderDateTo.getValue() : LocalDate.now());

				filters.put("ordersDate", orderDates);
			}

			if (!dueDateFrom.isEmpty()) {

				Map<String, LocalDate> dueDates = Maps.newHashMap();
				dueDates.put("dueDateFrom", dueDateFrom.getValue());
				dueDates.put("dueDateTo", !dueDateTo.isEmpty() ? dueDateTo.getValue() : LocalDate.now());

				filters.put("dueDates", dueDates);
			}

			List<Order> results = orderRepositoryCustom.filterBy(filters);

			ldp = DataProvider.ofCollection(results);
			grid.setItems(ldp);
			ldp.refreshAll();

			searchOrdersDialog.close();

		});

		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			searchOrdersDialog.close();
			refreshGrid();
		});

		IntegerField id = new IntegerField("Account Id");
		id.setVisible(false);

		Hr divider1 = new Hr();

		Hr divider2 = new Hr();
		Hr divider3 = new Hr();
		Hr divider4 = new Hr();
		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addProfileLabel, divider1, orderId, deliveryReceiptField, stockTransferField, invoiceNUmberField,
				divider3, storeName, orderStatusField, divider4, orderDateFrom, orderDateTo, dueDateFrom, dueDateTo,
				divider2, cancelButton, searchButton, id);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(addProfileLabel, 2);
		formLayout.setColspan(divider1, 2);
		formLayout.setColspan(divider2, 2);
		formLayout.setColspan(divider3, 2);
		formLayout.setColspan(divider4, 2);

		searchOrdersDialog.add(formLayout);

	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Stock Orders");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		addStockOrderButton = new Button("Create Stock Order");
		addStockOrderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addStockOrderButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
		addStockOrderButton.addClickListener(e -> {

			UI.getCurrent().navigate(CreateOrderFormView.class);
		});
		flexWrapper.add(addStockOrderButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		customers = customerService.listAll(Sort.by("id"));

		if (!customers.isEmpty()) {
			// customers.forEach(customer -> populateForm(customer));

		} else {
			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			// refreshGrid();
			event.forwardTo(StockOrderView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		grid.setSelectionMode(SelectionMode.MULTI);
		grid.addComponentColumn(order -> {

			VerticalLayout customerLayout = new VerticalLayout();
			customerLayout.setWidthFull();
			customerLayout.setSpacing(false);

			Customer customer = order.getCustomer();

			HorizontalLayout storeNameLayout = new HorizontalLayout();
			storeNameLayout.setWidthFull();
			Icon icon = new Icon(VaadinIcon.SHOP);
			icon.setClassName("grid-icons");
			Span storeIcon = new Span(icon);
			Span storeNameValue = new Span(customer.getStoreName());
			storeNameValue.setClassName("order-row-value-customer");
			storeNameLayout.add(storeIcon, storeNameValue);

			HorizontalLayout ownerNameLayout = new HorizontalLayout();
			ownerNameLayout.setWidthFull();
			Span ownerNameValue = new Span(customer.getOwnerName());
			ownerNameLayout.add(ownerNameValue);
			ownerNameLayout.setClassName("owner-row-secondary-text");

			HorizontalLayout storeAddressLayout = new HorizontalLayout();
			storeAddressLayout.setWidthFull();
			Span storeAddressValue = new Span(customer.getAddress());
			storeAddressLayout.add(storeAddressValue);
			storeAddressLayout.setClassName("owner-row-secondary-text");

			customerLayout.add(storeNameLayout, ownerNameLayout, storeAddressLayout);

			return customerLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Customer").setSortable(true)
				.setComparator(e -> {
					return e.getCustomer().getStoreName();
				});

		grid.addComponentColumn(order -> {

			VerticalLayout orderLayout = new VerticalLayout();
			orderLayout.setWidthFull();
			orderLayout.setSpacing(false);

			HorizontalLayout orderIdLayout = new HorizontalLayout();
			orderIdLayout.setWidthFull();
			Span orderIdSpan = new Span("Stock Order:");
			orderIdSpan.setClassName("order-row-label");
			Span orderIdValue = new Span(getStockOrderLink(order));
			orderIdValue.setClassName("order-row-value");

			orderIdLayout.add(orderIdSpan, orderIdValue);
			orderLayout.add(orderIdLayout);
			

			boolean isCompanyOwned = PfdiUtil.isRelativeOrCompanyOwned(order.getCustomer().getCustomerTagId());
			if (!isCompanyOwned) {

				HorizontalLayout deliveryReceiptLayout = new HorizontalLayout();
				deliveryReceiptLayout.setWidthFull();
				Span deliveryReceiptLabel = new Span("Delivery Receipt:");
				deliveryReceiptLabel.setClassName("order-row-label");
				Span deliveryReceiptValue = new Span(getDeliveryReceiptLink(order));
				deliveryReceiptValue.setClassName("order-row-value");
	
				deliveryReceiptLayout.add(deliveryReceiptLabel, deliveryReceiptValue);
				orderLayout.add(deliveryReceiptLayout);
				
				HorizontalLayout invoiceLayout = new HorizontalLayout();
				String label = "Invoice:";
				invoiceLayout.setWidthFull();
				Span invoiceLayoutLabel = new Span(label);
				invoiceLayoutLabel.setClassName("order-row-label");
				Span invoiceLayoutValue = new Span(getInvoiceLink(order, isCompanyOwned));
				invoiceLayoutValue.setClassName("order-row-value");

				invoiceLayout.add(invoiceLayoutLabel, invoiceLayoutValue);

				orderLayout.add(invoiceLayout);
			}

			if (isCompanyOwned) {

				HorizontalLayout stockTransferLayout = new HorizontalLayout();

				stockTransferLayout.setWidthFull();
				Span stockTransferLabel = new Span("Stock Transfer:");
				stockTransferLabel.setClassName("order-row-label");
				Span stockTransferValue = new Span(getStockTransferLink(order, isCompanyOwned));
				stockTransferValue.setClassName("order-row-value");

				stockTransferLayout.add(stockTransferLabel, stockTransferValue);

				orderLayout.add(stockTransferLayout);
			}

			return orderLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Order").setSortable(true)
				.setComparator(Order::getStockOrderNumber);

		grid.addComponentColumn(order -> {

			VerticalLayout orderDetailsLayout = new VerticalLayout();
			orderDetailsLayout.setWidthFull();
			orderDetailsLayout.setHeightFull();
			orderDetailsLayout.setSpacing(false);

			HorizontalLayout amountDueLayout = new HorizontalLayout();
			amountDueLayout.setWidthFull();
			Span amountDueValue = new Span("TP : " + PfdiUtil.getFormatter().format(order.getAmountDue()));
			amountDueValue.setClassName("order-row-value-amount");
			amountDueLayout.add(amountDueValue);

			HorizontalLayout srpLayout = new HorizontalLayout();
			srpLayout.setWidthFull();
			Span srp = new Span("SRP : " + PfdiUtil.getFormatter()
					.format(order.getAmountSrp() != null ? order.getAmountSrp() : BigDecimal.valueOf(0)));
			srpLayout.setClassName("order-row-value-amount");
			srpLayout.add(srp);
			srpLayout.setVisible(PfdiUtil.isCompanyOwned(order.getCustomer().getCustomerTagId()));

			HorizontalLayout orderedFrom = new HorizontalLayout();
			orderedFrom.setWidthFull();
			Span orderedFromValue = new Span("Sales Rep: " + order.getCreatedByUser().getFirstName() + " "
					+ order.getCreatedByUser().getLastName());
			orderedFrom.add(orderedFromValue);
			orderedFrom.setClassName("owner-row-secondary-text");

			HorizontalLayout orderDate = new HorizontalLayout();
			orderDate.setWidthFull();
			Span orderDateValue = new Span("Order Date : " + PfdiUtil.formatDateWithHours(order.getCreationDate()));
			orderDate.add(orderDateValue);
			orderDate.setClassName("owner-row-secondary-text");
			

			orderDetailsLayout.add(amountDueLayout, srpLayout, orderedFrom, orderDate);

			return orderDetailsLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Order Details").setSortable(true)
				.setComparator(e -> {
					return e.getAmountDue();
				});

		grid.addComponentColumn(order -> {

			VerticalLayout paymentDetailsLayout = new VerticalLayout();
			paymentDetailsLayout.setWidthFull();
			paymentDetailsLayout.setSpacing(false);

			List<Payment> payments = order.getPayments();

			HorizontalLayout paymentStatusWrapper = new HorizontalLayout();
			paymentStatusWrapper.setWidthFull();

			PaymentStatus paymentStatus = PaymentStatus.UNPAID;

			if (payments.isEmpty()) {
				paymentStatus = PaymentStatus.UNPAID;
			} else {
				boolean hasForVerification = payments.stream()
						.anyMatch(e -> PaymentStatus.FOR_VERIFICATION.name().equalsIgnoreCase(e.getStatus()));
				if (hasForVerification) {

					if (BigDecimal.ZERO.compareTo(order.getBalance()) == 0) {

						paymentStatus = PaymentStatus.FOR_VERIFICATION;
					} else {
						paymentStatus = PaymentStatus.PARTIAL_FOR_VERIFICATION;
					}
				} else if (BigDecimal.ZERO.compareTo(order.getBalance()) == 0) {
					paymentStatus = PaymentStatus.PAID;
				} else {
					paymentStatus = PaymentStatus.PARTIALLY_PAID;
				}
			}

			Span paymentStatusBadge = new Span(paymentStatus.getPaymentStatusName());
			paymentStatusBadge.getElement().getThemeList().add(paymentStatus.getBadge());
			paymentStatusWrapper.add(paymentStatusBadge);
			paymentDetailsLayout.add(paymentStatusWrapper);

			HorizontalLayout balanceWrapper = new HorizontalLayout();
			balanceWrapper.setWidthFull();
			Span balanceValue = new Span("Balance: " + order.getBalance());
			balanceWrapper.add(balanceValue);
			balanceWrapper.setClassName("owner-row-secondary-text");

			paymentDetailsLayout.add(balanceWrapper);

			HorizontalLayout dueDateWrapper = new HorizontalLayout();
			dueDateWrapper.setWidthFull();
			Span dueDateValue = new Span("Due Date: " + order.getDueDate().toString());
			dueDateWrapper.add(dueDateValue);
			dueDateWrapper.setClassName("owner-row-secondary-text");

			paymentDetailsLayout.add(dueDateWrapper);

			return paymentDetailsLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Payment Details").setSortable(true)
				.setComparator(e -> {
					return e.getCustomer().getStoreName();
				});

		grid.addComponentColumn(currentOrder -> {
			VerticalLayout statusDetails = new VerticalLayout();
			statusDetails.setWidthFull();
			statusDetails.setSpacing(false);
			
			OrderStatus status = PfdiUtil.getStatus(currentOrder.getStatus());
			
			HorizontalLayout lastUpdateByLayout = new HorizontalLayout();
			lastUpdateByLayout.setWidthFull();
			Span lastUpdateByValue = new Span("Last Updated By: " + PfdiUtil.getFullName(currentOrder.getUpdatedByUser()));
			lastUpdateByLayout.add(lastUpdateByValue);
			lastUpdateByLayout.setClassName("owner-row-secondary-text");
			
			HorizontalLayout lastUpdateDateLayout = new HorizontalLayout();
			lastUpdateDateLayout.setWidthFull();
			Span lastUpdateDateValue = new Span("Last Updated Date: " + PfdiUtil.formatDate(currentOrder.getUpdatedDate()));
			lastUpdateDateLayout.add(lastUpdateDateValue);
			lastUpdateDateLayout.setClassName("owner-row-secondary-text");
			
			statusDetails.add(status.getBadge(), lastUpdateByLayout, lastUpdateDateLayout);
			
			return statusDetails;
			
		}).setAutoWidth(true).setFlexGrow(0).setHeader("Status");
		grid.addColumn(Order::getNotes).setHeader("Notes").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		grid.addComponentColumn(currentOrder -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();

			MenuItem editItemSubMenu = subMenu.addItem("Edit Order", e -> {

				RouteParam param = new RouteParam("orderId", currentOrder.getId().toString());
				RouteParameters params = new RouteParameters(param);
				UI.getCurrent().navigate(CreateOrderFormView.class, params);
			});
			
			MenuItem setAsDelivered = subMenu.addItem("Set As Delivered", e -> {
				
				ConfirmDialog confirmDialog = new ConfirmDialog();
		      	
	        	confirmDialog.setCancelable(true);
	        	confirmDialog.setHeader("Are you sure that this order has already been delivered?");
	        	
	        	Button confirmButton = new Button("Confirm");
	        	confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);       	

	        	
	         	confirmButton.addClickListener(buttonClickListener -> {
	         		currentOrder.setStatus(OrderStatus.DELIVERED.getOrderStatusName());
	         		currentOrder.setDeliveryDate(LocalDateTime.now());

	         		currentOrder.setUpdatedByUser(appUser);
	         		currentOrder.setUpdatedDate(LocalDateTime.now());
	         		

	        		Order updatedOrder = ordersService.update(currentOrder);

					Notification.show("Stock Order #" + updatedOrder.getStockOrderNumber() + " has been delivered.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					refreshGrid();
					confirmDialog.close();
	        	
	        	});
	         	
	         	confirmDialog.setConfirmButton(confirmButton);
	        	confirmDialog.open();

			});
			
			MenuItem cancelOrder = subMenu.addItem("Cancel Order", e -> {
				
				ConfirmDialog confirmDialog = new ConfirmDialog();
		      	
	        	confirmDialog.setCancelable(true);
	        	confirmDialog.setHeader("Are you sure you want to cancel this order?");
	        	
	        	Button confirmButton = new Button("Confirm");
	        	confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);       	

	        	
	         	confirmButton.addClickListener(buttonClickListener -> {
	         		currentOrder.setStatus(OrderStatus.CANCELLED.getOrderStatusName());

	         		currentOrder.setUpdatedByUser(appUser);
	         		currentOrder.setUpdatedDate(LocalDateTime.now());
	         		

	        		Order updatedOrder = ordersService.update(currentOrder);

					Notification.show("Stock Order #" + updatedOrder.getStockOrderNumber() + " has been cancelled.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					refreshGrid();
					confirmDialog.close();
	        	
	        	});
	         	
	         	confirmDialog.setConfirmButton(confirmButton);
	        	confirmDialog.open();

			});

			if (currentOrder.getPayments() != null && BigDecimal.ZERO.equals(currentOrder.getBalance())
					|| OrderStatus.DELIVERED.getOrderStatusName().equalsIgnoreCase(currentOrder.getStatus())) {
				// addPaymentSubMenu.setEnabled(false);
				editItemSubMenu.setEnabled(false);
			}

			if (currentOrder.getPayments() != null && !currentOrder.getPayments().isEmpty()
					|| OrderStatus.DELIVERED.getOrderStatusName().equalsIgnoreCase(currentOrder.getStatus())) {
				editItemSubMenu.setEnabled(false);
			}
			
			if (OrderStatus.DELIVERED.getOrderStatusName().equalsIgnoreCase(currentOrder.getStatus()) 
					|| OrderStatus.CANCELLED.getOrderStatusName().equalsIgnoreCase(currentOrder.getStatus())) {
				setAsDelivered.setEnabled(false);
			}
			
			if (OrderStatus.DELIVERED.getOrderStatusName().equalsIgnoreCase(currentOrder.getStatus()) 
					|| OrderStatus.CANCELLED.getOrderStatusName().equalsIgnoreCase(currentOrder.getStatus())
					|| currentOrder.getPayments().size() > 0) {
				cancelOrder.setEnabled(false);
			}


			return menuBar;
		}).setWidth("70px").setFlexGrow(0);
		
		Map<String, Object> filters = Maps.newHashMap();
		
		Map<String, LocalDate> orderDates = Maps.newHashMap();
		
		
		orderDates.put("orderDateFrom", LocalDate.now().with(DayOfWeek.MONDAY));
		orderDates.put("orderDateTo", LocalDate.now().with(DayOfWeek.SUNDAY));
		
		filters.put("ordersDate", orderDates);
		orders = orderRepositoryCustom.filterBy(filters);
		

		ldp = DataProvider.ofCollection(orders);

		Button printAllButton = new Button(new Icon(VaadinIcon.PRINT));
		printAllButton.setTooltipText("Print All Documents");
		printAllButton.setEnabled(false);

		MenuBar options = new MenuBar();

		options.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);

		MenuItem printMenu = options.addItem(new Icon(VaadinIcon.PRINT));

		SubMenu printSubMenu = printMenu.getSubMenu();
		
		MenuItem printOptions = printSubMenu.addItem("Print All", e -> {

			List<String> orderIds = grid.getSelectedItems().stream().map(order -> order.getId().toString())
					.collect(Collectors.toList());
			
			RouteParam idParam = new RouteParam("id", String.join(",", orderIds));

			RouteParam report = new RouteParam("report", Reports.ALL.name());
			RouteParameters parameters = new RouteParameters(idParam, report);

			UI.getCurrent().navigate(PrinterView.class, parameters);

		});

		MenuItem printAllStockOrders = printSubMenu.addItem("Print Stock Orders", e -> {

			List<String> orderIds = grid.getSelectedItems().stream().map(order -> order.getId().toString())
					.collect(Collectors.toList());
			RouteParam idParam = new RouteParam("id", String.join(",", orderIds));

			RouteParam report = new RouteParam("report", Reports.SO.name());
			RouteParameters parameters = new RouteParameters(idParam, report);

			UI.getCurrent().navigate(PrinterView.class, parameters);

		});

		MenuItem printAllDeliveryReceipts = printSubMenu.addItem("Print Delivery Receipts", e -> {

			List<String> orderIds = grid.getSelectedItems().stream().map(order -> order.getId().toString())
					.collect(Collectors.toList());
			RouteParam idParam = new RouteParam("id", String.join(",", orderIds));

			RouteParam report = new RouteParam("report", Reports.DR.name());
			RouteParameters parameters = new RouteParameters(idParam, report);

			UI.getCurrent().navigate(PrinterView.class, parameters);

		});

		MenuItem printAllInvoices = printSubMenu.addItem("Print Invoices", e -> {

			List<String> orderIds = grid.getSelectedItems().stream().map(order -> order.getId().toString())
					.collect(Collectors.toList());
			RouteParam idParam = new RouteParam("id", String.join(",", orderIds));

			RouteParam report = new RouteParam("report", Reports.SI.name());
			RouteParameters parameters = new RouteParameters(idParam, report);

			UI.getCurrent().navigate(PrinterView.class, parameters);

		});

		MenuItem printAllStockTransfers = printSubMenu.addItem("Print Stock Transfers", e -> {

			List<String> orderIds = grid.getSelectedItems().stream().map(order -> order.getId().toString())
					.collect(Collectors.toList());
			RouteParam idParam = new RouteParam("id", String.join(",", orderIds));

			RouteParam report = new RouteParam("report", Reports.ST.name());
			RouteParameters parameters = new RouteParameters(idParam, report);

			UI.getCurrent().navigate(PrinterView.class, parameters);

		});

		printOptions.setEnabled(false);
		printAllStockOrders.setEnabled(false);
		printAllDeliveryReceipts.setEnabled(false);
		printAllInvoices.setEnabled(false);
		printAllStockTransfers.setEnabled(false);
		MenuItem optionsMenu = options.addItem(new Icon(VaadinIcon.COG));

		SubMenu optionsMenuSubItems = optionsMenu.getSubMenu();

		MenuItem statusMenuItem = optionsMenuSubItems.addItem("Set Order Status");
		
		optionsMenuSubItems.addItem(new Hr());

		MenuItem rowsPerPage = optionsMenuSubItems.addItem("Rows per page");

		SubMenu statusSubMenu = statusMenuItem.getSubMenu();

		MenuItem forDeliveryMenuItem = statusSubMenu.addItem("For Delivery", e -> {

			Set<Order> orders = grid.getSelectedItems();

			orders.forEach(order -> {

				if (!order.getStatus().equals(OrderStatus.DELIVERED.getOrderStatusName())) {
					Notification
							.show("Order " + order.getStockOrderNumber()
									+ " already delivered. Status could not be set to For Delivery.")
							.addThemeVariants(NotificationVariant.LUMO_ERROR);
					order.setStatus(OrderStatus.FOR_DELIVERY.getOrderStatusName());
				}
			});

			if (!orders.isEmpty()) {
				ordersService.updateAll(orders);
			}

			Notification.show("Selected Orders successfully set to delivered.")
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

			grid.refreshPaginator();
			grid.getDataProvider().refreshAll();
		});
		forDeliveryMenuItem.setEnabled(false);

		MenuItem cancelOrdersMenuItem = statusSubMenu.addItem("Cancelled", e -> {

			Set<Order> orders = grid.getSelectedItems();
			// TODO change the
			boolean othersAlreadyDelivered = false;
			for (Order order : orders) {

				if (!order.getStatus().equals(OrderStatus.DELIVERED.getOrderStatusName())) {
					othersAlreadyDelivered = true;
					order.setStatus(OrderStatus.CANCELLED.getOrderStatusName());
				}

			}

			if (!orders.isEmpty()) {
				ordersService.updateAll(orders);
			}

			if (othersAlreadyDelivered) {
				Notification.show("Selected Orders successfully set to cancelled.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} else {
				Notification.show(
						"Orders successfully set to cancelled. Some orders could not be set to cancelled since those have already been delivered.")
						.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

			}

			grid.refreshPaginator();
			grid.getDataProvider().refreshAll();
		});
		cancelOrdersMenuItem.setEnabled(false);

		MenuItem setToDeliveryItem = statusSubMenu.addItem("Delivered", e -> {

			Set<Order> orders = grid.getSelectedItems();

			orders.forEach(order -> order.setStatus(OrderStatus.DELIVERED.getOrderStatusName()));

			if (!orders.isEmpty()) {
				ordersService.updateAll(orders);
			}

			Notification.show("Selected Orders successfully set to delivered.")
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

			grid.refreshPaginator();
			grid.getDataProvider().refreshAll();
		});
		setToDeliveryItem.setEnabled(false);

		SubMenu rowsPerPageSubItem = rowsPerPage.getSubMenu();
		rowsPerPageSubItem.addItem("10", e -> grid.setPageSize(10));
		rowsPerPageSubItem.addItem("15", e -> grid.setPageSize(15));
		rowsPerPageSubItem.addItem("20", e -> grid.setPageSize(20));
		rowsPerPageSubItem.addItem("50", e -> grid.setPageSize(50));
		

		
		MenuItem manageOfferings = optionsMenuSubItems.addItem("Manage Offerings");
		manageOfferings.addClickListener(e-> {
			UI.getCurrent().navigate(OfferingsView.class);
		});

		grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES,
				GridVariant.MATERIAL_COLUMN_DIVIDERS);
		grid.setVerticalScrollingEnabled(false);
		grid.addSelectionListener(e -> {
			Set<Order> selectedItems = grid.getSelectedItems();
			printOptions.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			printAllButton.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			setToDeliveryItem.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			cancelOrdersMenuItem.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			forDeliveryMenuItem.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			

			printAllStockOrders.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			printAllDeliveryReceipts.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			printAllInvoices.setEnabled(selectedItems != null && !selectedItems.isEmpty());
			printAllStockTransfers.setEnabled(selectedItems != null && !selectedItems.isEmpty());
		});

		// Sets the max number of items to be rendered on the grid for each page
		grid.setPageSize(10);

		// Sets how many pages should be visible on the pagination before and/or after
		// the current selected page
		grid.setPaginatorSize(5);

		grid.setHeight("90%");

		Button searchButton = new Button("Search Orders", new Icon(VaadinIcon.SEARCH));
		searchButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS);
		searchButton.setIconAfterText(true);
		searchButton.addClickListener(e -> searchOrdersDialog.open());

		HorizontalLayout printButtonContainer = new HorizontalLayout();
		printButtonContainer.setJustifyContentMode(JustifyContentMode.END);
		printButtonContainer.setPadding(false);
		printButtonContainer.setWidth("30%");
		printButtonContainer.add(options);

		HorizontalLayout searchFiltersLayout = new HorizontalLayout();
		searchFiltersLayout.addClassName("padding-bottom-medium");
		searchFiltersLayout.setWidth("70%");
		searchFiltersLayout.add(searchButton);

		HorizontalLayout secondaryActionsLayout = new HorizontalLayout();
		secondaryActionsLayout.setWidthFull();
		secondaryActionsLayout.add(searchFiltersLayout, printButtonContainer);

		wrapper.add(secondaryActionsLayout, new Hr(), grid);
		verticalLayout.add(wrapper);
	}

	private Component getStockTransferLink(Order order, boolean isCompanyOwned) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());

		String path = "#" + order.getStockTransferId();

		String route = RouteConfiguration.forSessionScope().getUrl(StockTransferView.class, parameters);
		Anchor anchor = new Anchor(route, path);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = order.getStockTransferId() != null ? anchor : span;
		return component;
	}

	private Component getInvoiceLink(Order order, boolean isCompanyOwned) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());

		String path = "#" + order.getInvoiceId();

		String route = RouteConfiguration.forSessionScope().getUrl(SalesInvoiceView.class, parameters);
		Anchor anchor = new Anchor(route, path);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = order.getInvoiceId() != null ? anchor : span;
		return component;

	}

	private Component getDeliveryReceiptLink(Order order) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		String deliveryReceipt = order.getDeliveryReceiptId() != null ? "#" + order.getDeliveryReceiptId() : "N/A";
		String route = RouteConfiguration.forSessionScope().getUrl(DeliveryReceiptReportView.class, parameters);

		Anchor anchor = new Anchor(route, deliveryReceipt);
		anchor.setClassName("order-row-value");
		Span span = new Span("N/A");
		span.setClassName("order-row-value");
		Component component = order.getDeliveryReceiptId() != null ? anchor : span;
		return component;
	}

	private Component getStockOrderLink(Order order) {
		RouteParameters parameters = new RouteParameters("id", order.getId().toString());
		String route = RouteConfiguration.forSessionScope().getUrl(StockOrderSummaryView.class, parameters);

		Integer stockOrderNumber = order.getStockOrderNumber();
		String pathString = stockOrderNumber != null ? stockOrderNumber.toString() : "N/A";
		String path = stockOrderNumber != null ? "#" + pathString : "N/A";
		;
		Anchor component = new Anchor(route, path);
		component.setClassName("order-row-value");
		return component;
	}

	private void refreshGrid() {
		if (grid != null) {
			//grid.select(null);
			grid.getListDataView().refreshAll();
		}
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

}
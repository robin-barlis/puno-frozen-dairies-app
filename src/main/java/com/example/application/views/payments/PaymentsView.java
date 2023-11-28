
package com.example.application.views.payments;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.vaadin.klaudeta.PaginatedGrid;

import com.example.application.data.CustomerPaymentStatus;
import com.example.application.data.PaymentMode;
import com.example.application.data.PaymentStatus;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrderRepositoryCustomImpl;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.PaymentRepositoryCustomImpl;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.data.service.products.SizesService;
import com.example.application.reports.OrderSummaryReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.utils.service.ReportConsolidatorService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.order.DeliveryReceiptReportView;
import com.example.application.views.order.SalesInvoiceView;
import com.example.application.views.order.StockOrderSummaryView;
import com.example.application.views.order.StockTransferView;
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
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
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

@PageTitle("Customer Payments")
@Route(value = "payments", layout = MainLayout.class)
@RouteAlias(value = "/payments", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Accounting", "ACCOUNTING", "Sales", "SALES" })
@Uses(Icon.class)
public class PaymentsView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	PaginatedGrid<Payment, ?> grid = new PaginatedGrid<>();
	private Button createPaymentButton;

	private OrdersService ordersService;
	private final CustomerService customerService;

	private ListDataProvider<Payment> ldp = null;
	List<Payment> payments;
	List<Customer> customers;
	private Dialog searchOrdersDialog = new Dialog();
	private PaymentsService paymentsService;

	private PaymentRepositoryCustomImpl paymentRepositoryCustom;

	@Autowired
	public PaymentsView(OrdersService ordersService, CustomerService customerService, AuthenticatedUser user,
			OrderSummaryReport orderSummaryReport, SizesService sizesService,
			OrderRepositoryCustomImpl orderRepositoryCustom, PaymentRepositoryCustomImpl paymentRepositoryCustom,
			ReportConsolidatorService reportConsolidatorService,
			PaymentsService paymentsService) {
		super("Admin", "Admin");
		this.customerService = customerService;
		this.ordersService = ordersService;
		this.paymentsService = paymentsService;
		this.paymentRepositoryCustom = paymentRepositoryCustom;
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);
		createSearchDialog("Search Payments");

		add(tableContent);

	}

	private void createSearchDialog(String label) {
		Label searchPaymentDialogLabel = new Label(label);
		searchPaymentDialogLabel.getStyle().set("padding-bottom", "20px");

		NumberField orderId = new NumberField("Stock Order Number");

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

		MultiSelectComboBox<PaymentMode> paymentModeField = new MultiSelectComboBox<>();


		List<PaymentMode> paymentMode = Arrays.asList(PaymentMode.values());
		paymentModeField.setLabel("Payment Mode");
		paymentModeField.setItems(paymentMode);
		paymentModeField.setPlaceholder("Select Payment Mode");
		paymentModeField.setItemLabelGenerator(e -> e.getName());

		DatePicker paymentDateTo = new DatePicker("Payment Date To");
		paymentDateTo.setEnabled(false);

		DatePicker paymentDateFrom = new DatePicker("Payment Date From");
		paymentDateFrom.addValueChangeListener(e -> {
			paymentDateTo.setValue(LocalDate.now());
			paymentDateTo.setEnabled(true);
		});


		Button searchButton = new Button("Search");
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.addClickListener(e -> {

			Map<String, Object> filters = Maps.newHashMap();

			if (!orderId.isEmpty()) {
				filters.put("stockOrderNumber", orderId.getValue());
			}

			if (!storeName.isEmpty()) {
				filters.put("store", storeName.getValue());
			}

			if (!paymentModeField.isEmpty()) {

				List<String> paymendModeName = paymentModeField.getValue().stream().map(val -> val.name())
						.collect(Collectors.toList());
				filters.put("paymentMode", paymendModeName);
			}

			if (!paymentDateFrom.isEmpty()) {

				Map<String, LocalDate> orderDates = Maps.newHashMap();
				orderDates.put("paymentDateFrom", paymentDateFrom.getValue());
				orderDates.put("paymentDateTo", !paymentDateTo.isEmpty() ? paymentDateTo.getValue() : LocalDate.now());

				filters.put("paymentDate", orderDates);
			}

			List<Payment> results = paymentRepositoryCustom.filterBy(filters);

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
		formLayout.add(searchPaymentDialogLabel, orderId, storeName, paymentModeField, divider1, 
				paymentDateFrom, paymentDateTo,
				divider2, cancelButton, searchButton, id);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(searchPaymentDialogLabel, 2);
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
		H1 header = new H1("Customer Payments");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		createPaymentButton = new Button("Create Payment");
		createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createPaymentButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
		createPaymentButton.addClickListener(e -> {

			UI.getCurrent().navigate(CreatePaymentView.class);
		});
		flexWrapper.add(createPaymentButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper, new Hr());
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		List<Payment> payments = paymentsService.listAll(Sort.by("id"));

		if (!payments.isEmpty()) {
			//customers.forEach(customer -> populateForm(customer));

		} else {
			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			refreshGrid();
			event.forwardTo(PaymentsView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		//grid.setSelectionMode(SelectionMode.MULTI);
		
		grid.addComponentColumn(payment -> {

			VerticalLayout orderDetailsLayout = new VerticalLayout();
			orderDetailsLayout.setWidthFull();
			orderDetailsLayout.setHeightFull();
			orderDetailsLayout.setSpacing(false);

			HorizontalLayout paymentDateLayout = new HorizontalLayout();
			paymentDateLayout.setWidthFull();
			Icon icon = new Icon(VaadinIcon.CALENDAR);
			icon.setClassName("grid-icons");
			Span iconSpan = new Span(icon);
			Span paymentDateValue = new Span(PfdiUtil.formatDate(payment.getPaymentDate()));
			paymentDateValue.setClassName("order-row-value-customer");
			paymentDateLayout.add(iconSpan, paymentDateValue);
			
			HorizontalLayout paymentMode = new HorizontalLayout();
			paymentMode.setWidthFull();
			Span paymentModeValue = new Span(PaymentMode.valueOf(payment.getPaymentMode()).getName());
			paymentModeValue.setClassName("order-row-value-customer");
			paymentMode.add(paymentModeValue);

			HorizontalLayout paidTo = new HorizontalLayout();
			paidTo.setWidthFull();
			Span paidToValue = new Span("Processed By: " + payment.getOrderId().getCreatedByUser().getFirstName()
					+ " " + payment.getOrderId().getCreatedByUser().getLastName());
			paidTo.add(paidToValue);
			paidTo.setClassName("owner-row-secondary-text");

			orderDetailsLayout.add(paymentDateLayout, paymentMode, paidTo);

			return orderDetailsLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Payment").setSortable(true)
				.setComparator(e -> {
					return e.getOrderId().getAmountDue();
				});

		grid.addComponentColumn(payment -> {

			VerticalLayout customerLayout = new VerticalLayout();
			customerLayout.setWidthFull();
			customerLayout.setSpacing(false);

			Customer customer = payment.getCustomer();

			HorizontalLayout storeNameLayout = new HorizontalLayout();
			storeNameLayout.setWidthFull();
			Span storeNameValue = new Span(customer.getStoreName());
			storeNameValue.setClassName("order-row-value-customer");
			storeNameLayout.add(storeNameValue);

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

		grid.addComponentColumn(payment -> {

			VerticalLayout orderLayout = new VerticalLayout();
			orderLayout.setWidthFull();
			orderLayout.setSpacing(false);

			HorizontalLayout orderIdLayout = new HorizontalLayout();
			orderIdLayout.setWidthFull();
			Span orderIdSpan = new Span("Stock Order:");
			orderIdSpan.setClassName("order-row-label");
			Order order = payment.getOrderId();
			Span orderIdValue = new Span(getStockOrderLink(order));
			orderIdValue.setClassName("order-row-value");

			orderIdLayout.add(orderIdSpan, orderIdValue);
			orderLayout.add(orderIdLayout);
			

			boolean isCompanyOwned = PfdiUtil.isRelativeOrCompanyOwned(payment.getCustomer().getCustomerTagId());
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
				.setComparator(payment -> {
					return payment.getOrderId().getStockOrderNumber();
				});
		
		
//		grid.addComponentColumn(payment -> {
//
//			VerticalLayout orderDetailsLayout = new VerticalLayout();
//			orderDetailsLayout.setWidthFull();
//			orderDetailsLayout.setHeightFull();
//			orderDetailsLayout.setSpacing(false);
//
//			HorizontalLayout amountDueLayout = new HorizontalLayout();
//			amountDueLayout.setWidthFull();
//			Span amountDueValue = new Span("TP : " + PfdiUtil.getFormatter().format(payment.getOrderId().getAmountDue()));
//			amountDueValue.setClassName("order-row-value-amount");
//			amountDueLayout.add(amountDueValue);
//
//
//			HorizontalLayout srpLayout = new HorizontalLayout();
//			srpLayout.setWidthFull();
//			Span srp = new Span("SRP : " + PfdiUtil.getFormatter()
//					.format(payment.getOrderId().getAmountSrp() != null ? payment.getOrderId().getAmountSrp() : BigDecimal.valueOf(0)));
//			srpLayout.setClassName("order-row-value-amount");
//			srpLayout.add(srp);
//			srpLayout.setVisible(PfdiUtil.isCompanyOwned(payment.getCustomer().getCustomerTagId()));
//
//			HorizontalLayout orderedFrom = new HorizontalLayout();
//			orderedFrom.setWidthFull();
//			Span orderedFromValue = new Span("Sales Rep: " + payment.getOrderId().getCreatedByUser().getFirstName() + " "
//					+ payment.getOrderId().getCreatedByUser().getLastName());
//			orderedFrom.add(orderedFromValue);
//			orderedFrom.setClassName("owner-row-secondary-text");
//
//			HorizontalLayout orderDate = new HorizontalLayout();
//			orderDate.setWidthFull();
//			Span orderDateValue = new Span("Order Date : " + PfdiUtil.formatDateWithHours(payment.getOrderId().getCreationDate()));
//			orderDate.add(orderDateValue);
//			orderDate.setClassName("owner-row-secondary-text");
//			
//
//			orderDetailsLayout.add(amountDueLayout, srpLayout, orderedFrom, orderDate);
//
//			return orderDetailsLayout;
//		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Order Details").setSortable(true)
//				.setComparator(e -> {
//					return e.getOrderId().getAmountDue();
//				});
		
		grid.addComponentColumn(payment -> {

			VerticalLayout paymentDetailsLayout = new VerticalLayout();
			paymentDetailsLayout.setWidthFull();
			paymentDetailsLayout.setSpacing(false);

			HorizontalLayout paymentStatusWrapper = new HorizontalLayout();
			paymentStatusWrapper.setWidthFull();

			CustomerPaymentStatus paymentStatus = (BigDecimal.ZERO.compareTo(payment.getOrderId().getBalance()) == 0) ?
					CustomerPaymentStatus.FULL_PAYMENT : CustomerPaymentStatus.PARTIAL_PAYMENT ;

			Span paymentStatusBadge = paymentStatus.getBadge(payment.getAmount());
			paymentStatusWrapper.add(paymentStatusBadge);
			paymentDetailsLayout.add(paymentStatusWrapper);

			HorizontalLayout statusWrapper = new HorizontalLayout();
			statusWrapper.setWidthFull();
			Span status = new Span("Status: " + paymentStatus.getOrderStatusName());
			statusWrapper.add(status);
			statusWrapper.setClassName("owner-row-secondary-text");

			paymentDetailsLayout.add(statusWrapper);

			HorizontalLayout dueDateWrapper = new HorizontalLayout();
			dueDateWrapper.setWidthFull();
			Span dueDateValue = new Span("Current Balance: " + payment.getOrderId().getBalance());
			dueDateWrapper.add(dueDateValue);
			dueDateWrapper.setClassName("owner-row-secondary-text");

			paymentDetailsLayout.add(dueDateWrapper);

			return paymentDetailsLayout;
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Payment Details").setSortable(true)
				.setComparator(e -> {
					return e.getPaymentDate();
				});

		//grid.addColumn("notes").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		
		grid.addColumn(payment -> {
			return payment.getNote();
		}).setHeader("Notes").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
		grid.addComponentColumn(currentPayment -> {

			MenuBar menuBar = new MenuBar();
			menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
			MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
			menuItem.getElement().setAttribute("aria-label", "More options");
			SubMenu subMenu = menuItem.getSubMenu();

			subMenu.addItem("Edit Payment", e -> {

				RouteParam param = new RouteParam("paymentId", currentPayment.getId().toString());
				RouteParameters params = new RouteParameters(param);
				UI.getCurrent().navigate(CreatePaymentView.class, params);
			});   
			
			subMenu.addItem("Delete Payment", e -> {
				
				ConfirmDialog dialog = new ConfirmDialog();
				dialog.setHeader("Delete Payment?");
				dialog.setText(
				        "This cannot be reversed. Are you sure you want to permanently delete this item?");

				dialog.setCancelable(true);

				dialog.setConfirmText("Delete");
				dialog.setConfirmButtonTheme("error primary");
				dialog.addConfirmListener(event -> {
					Order order = currentPayment.getOrderId();
					BigDecimal currentBalance = order.getBalance();
					BigDecimal balanceAfterDelete = currentBalance.add(currentPayment.getAmount());
					order.setBalance(balanceAfterDelete);

					ordersService.update(order);
					paymentsService.delete(currentPayment.getId());
					refreshGrid();
					Notification notification = Notification
					        .show("Payment Successfullly deleted. Order balance reverted to amount before the payment was made.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					notification.setPosition(Position.BOTTOM_START);
					
					
				});

				dialog.open();
				
				
			});


			return menuBar;
		}).setWidth("70px").setFlexGrow(0);

		
		Map<String, Object> filters = Maps.newHashMap();
		
		Map<String, LocalDate> orderDates = Maps.newHashMap();
		
		
		orderDates.put("paymentDateFrom", LocalDate.now().with(DayOfWeek.MONDAY));
		orderDates.put("paymentDateTo", LocalDate.now().with(DayOfWeek.SUNDAY));
		
		filters.put("paymentDate", orderDates);
		payments = paymentRepositoryCustom.filterBy(filters);
		

		ldp = DataProvider.ofCollection(payments);


		Button printAllButton = new Button(new Icon(VaadinIcon.PRINT));
		printAllButton.setTooltipText("Print All Documents");
		printAllButton.setEnabled(false);

		MenuBar options = new MenuBar();

		options.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
	

		MenuItem optionsMenu = options.addItem(new Icon(VaadinIcon.COG));

		SubMenu optionsMenuSubItems = optionsMenu.getSubMenu();


		MenuItem rowsPerPage = optionsMenuSubItems.addItem("Rows per page");

		SubMenu rowsPerPageSubItem = rowsPerPage.getSubMenu();
		rowsPerPageSubItem.addItem("10", e -> grid.setPageSize(10));
		rowsPerPageSubItem.addItem("15", e -> grid.setPageSize(15));
		rowsPerPageSubItem.addItem("20", e -> grid.setPageSize(20));
		rowsPerPageSubItem.addItem("50", e -> grid.setPageSize(50));

		grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES,
				GridVariant.MATERIAL_COLUMN_DIVIDERS);
		grid.setVerticalScrollingEnabled(false);
//		grid.addSelectionListener(e -> {
//			Set<Order> selectedItems = grid.getSelectedItems();
//			printOptions.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			printAllButton.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			setToDeliveryItem.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			cancelOrdersMenuItem.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			forDeliveryMenuItem.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			
//
//			printAllStockOrders.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			printAllDeliveryReceipts.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			printAllInvoices.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//			printAllStockTransfers.setEnabled(selectedItems != null && !selectedItems.isEmpty());
//		});

		// Sets the max number of items to be rendered on the grid for each page
		grid.setPageSize(10);

		// Sets how many pages should be visible on the pagination before and/or after
		// the current selected page
		grid.setPaginatorSize(5);

		grid.setHeight("90%");

		Button searchButton = new Button("Search Payments", new Icon(VaadinIcon.SEARCH));
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
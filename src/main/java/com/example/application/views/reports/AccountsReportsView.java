package com.example.application.views.reports;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.AccountsReportType;
import com.example.application.data.SoaPeriodType;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.UserService;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrderRepositoryCustom;
import com.example.application.data.service.orders.OrderRepositoryCustomImpl;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.PaymentRepositoryCustom;
import com.example.application.data.service.payment.PaymentRepositoryCustomImpl;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.reports.OnlinePaymentSummaryReport;
import com.example.application.reports.OutstandingChequeSummaryReport;
import com.example.application.reports.RemittancesReport;
import com.example.application.reports.SubsidiaryLedgerReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@PageTitle("Stock Orders")
@Route(value = "reports/accounts/", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class AccountsReportsView extends AbstractPfdiView {


	private static final long serialVersionUID = 2754507440441771890L;

	private byte[] stockTransferData;

	private VerticalLayout mainDiv = new VerticalLayout();
	private VerticalLayout reportContainer = new VerticalLayout();

	private VerticalLayout formContainer = new VerticalLayout();
	private AuthenticatedUser authenticatedUser;
    private OutstandingChequeSummaryReport chequeSummaryReport;

    private OnlinePaymentSummaryReport onlinePaymentSummaryReport;
    
    private SubsidiaryLedgerReport subsidiaryLedgerReport;
    
	private PaymentRepositoryCustom paymentRepositoryCustom;


	private ComboBox<AccountsReportType> accountsReportType;
	private CustomerService customerService;
	private UserService userService;

	private Select<Customer> outletNameComboBox;

	private ArrayList<Customer> availableCustomers = Lists.newArrayList();
	
	private DatePicker startDate;
	private DatePicker endDate;
	
	private DatePicker startDateSoa;
	private DatePicker endDateSoa;

	private Select<SoaPeriodType> periodType;

	private ComboBox<AppUser> checkedBy;

	private ComboBox<AppUser> approvedBy;

	private OrderRepositoryCustom orderRepositoryCustom;


	@Autowired
	public AccountsReportsView(OrdersService ordersService, 
			PaymentsService paymentsService, 
			PaymentRepositoryCustomImpl paymentRepositoryCustom,
			OrderRepositoryCustomImpl orderRepositoryCustom,
			OutstandingChequeSummaryReport chequeSummaryReport,
			AuthenticatedUser authenticatedUser,
			CustomerService customerService,
			UserService userService, 
			OnlinePaymentSummaryReport onlinePaymentSummaryReport,
			SubsidiaryLedgerReport subsidiaryLedgerReport) {
		super("products-view", "Remittances");
		this.paymentRepositoryCustom = paymentRepositoryCustom;
		this.chequeSummaryReport = chequeSummaryReport;
		this.authenticatedUser = authenticatedUser;
		this.customerService = customerService;
		this.userService = userService;
		this.onlinePaymentSummaryReport = onlinePaymentSummaryReport;
		this.subsidiaryLedgerReport = subsidiaryLedgerReport;
		this.orderRepositoryCustom = orderRepositoryCustom;
		addClassNames("administration-view");
		reportContainer.setVisible(false);
		//populateReport(reportContainer);
		populateForm(formContainer);
		mainDiv.add(reportContainer);
		mainDiv.setHeightFull();

		add(formContainer, mainDiv);
	}
	
	private void populateForm(VerticalLayout formContainer2) {
		
		
		LocalDate currentFrom = LocalDate.now().withDayOfMonth(1); // first day
		LocalDate currentTo =LocalDate.now();
		
		HorizontalLayout optionsContainer = new HorizontalLayout();
		optionsContainer.setJustifyContentMode(JustifyContentMode.END);
		optionsContainer.setPadding(false);
		optionsContainer.setWidth("100%");
		
		HorizontalLayout datePicker = new HorizontalLayout();
		datePicker.setWidthFull();
		datePicker.setVisible(false);
		
		endDate = new DatePicker("End Date");
		endDate.setRequiredIndicatorVisible(true);
		endDate.setValue(currentTo);
		startDate = new DatePicker("Start Date");
		startDate.setRequiredIndicatorVisible(true);
		startDate.setValue(currentFrom);
		
		endDate.setWidthFull();
		endDate.setMax(LocalDate.now());
		endDate.addValueChangeListener(e-> {
			startDate.setMax(endDate.getValue());
		});
		
		startDate.setWidthFull();
		startDate.addValueChangeListener(e -> {
			endDate.setMin(e.getValue());
			endDate.setEnabled(true);
		});
		
		datePicker.add(startDate, endDate);
		
		
		endDateSoa = new DatePicker("End Date");
		endDateSoa.setRequiredIndicatorVisible(true);
		endDateSoa.setValue(currentTo);
		startDateSoa = new DatePicker("Start Date");
		startDateSoa.setValue(currentFrom);
		startDateSoa.setRequiredIndicatorVisible(true);
		
		endDateSoa.setWidthFull();
		endDateSoa.setMax(LocalDate.now());
		endDateSoa.addValueChangeListener(e-> {
			startDateSoa.setMax(endDateSoa.getValue());
		});
		
		startDateSoa.setWidthFull();
		startDateSoa.addValueChangeListener(e -> {
			endDateSoa.setMin(e.getValue());
			endDateSoa.setEnabled(true);
		});
		
		HorizontalLayout soaDateContainer = new HorizontalLayout();
		soaDateContainer.setWidth("50%");
		soaDateContainer.add(startDateSoa, endDateSoa);
		soaDateContainer.setVisible(false);
		
		
		HorizontalLayout soaLayout = new HorizontalLayout();
		soaLayout.setWidthFull();
		soaLayout.setVisible(false);
		
		
		periodType = new Select<>();
		periodType.setLabel("Period Type");
		periodType.setWidth("50%");
		periodType.setRequiredIndicatorVisible(true);
		periodType.setItems(SoaPeriodType.values());
		periodType.setItemLabelGenerator(SoaPeriodType::getLabel);
		periodType.addValueChangeListener(e -> {
			
			soaDateContainer.setVisible(e.getValue() == SoaPeriodType.DATE_RANGE);
			
		});
		
		soaLayout.add(periodType, soaDateContainer);
		
		List<AppUser> users = userService.listAll(Sort.unsorted());
		



		checkedBy = new ComboBox<AppUser>("Checked By");
		checkedBy.setItems(users);
		checkedBy.setItemLabelGenerator(appUser -> PfdiUtil.getFullName(appUser));
		checkedBy.setWidth("50%");

		approvedBy = new ComboBox<AppUser>("Approved By");
		approvedBy.setItems(users);
		approvedBy.setItemLabelGenerator(appUser -> PfdiUtil.getFullName(appUser));
		approvedBy.setWidth("50%");

		HorizontalLayout soaLayoutUserForms = new HorizontalLayout();
		soaLayoutUserForms.setWidthFull();
		soaLayoutUserForms.add(checkedBy, approvedBy);		
		soaLayoutUserForms.setVisible(false);
		
		
		
		accountsReportType = new ComboBox<AccountsReportType>("Report Type");
		accountsReportType.setItems(Lists.newArrayList(AccountsReportType.values()));
		accountsReportType.setWidth("50%");
		accountsReportType.setRequiredIndicatorVisible(true);
		accountsReportType.setItemLabelGenerator(AccountsReportType::getReportName);
		accountsReportType.addValueChangeListener(e -> {
			AccountsReportType reportType = e.getValue();
			
			if (reportType.equals(AccountsReportType.ONLINE_PAYMENT_SUMMARY)) {
				datePicker.setVisible(true);
				soaLayout.setVisible(false);
				soaLayoutUserForms.setVisible(false);
			} else if (reportType.equals(AccountsReportType.OUTSTANDING_CHEQUE_SUMMARY)) {
				datePicker.setVisible(true);
				soaLayout.setVisible(false);
				soaLayoutUserForms.setVisible(false);
			} else if (reportType.equals(AccountsReportType.STATEMENT_OF_ACCOUNT)) {
				soaLayout.setVisible(true);
				soaLayoutUserForms.setVisible(true);
				datePicker.setVisible(false);
			} else if (reportType.equals(AccountsReportType.SUBSIDIARY_LEDGER)) {
				datePicker.setVisible(true);
				soaLayout.setVisible(false);
				soaLayoutUserForms.setVisible(false);
				
			}
		});
		
		
		Map<String, List<Customer>> customerPerCategory = customerService.listAllByCustomerTag();
		for (Entry<String, List<Customer>> entrySet : customerPerCategory.entrySet()) {
			availableCustomers.addAll(entrySet.getValue());
		}
		
		outletNameComboBox = new Select<Customer>();
		outletNameComboBox.setItems(availableCustomers);
		for (Entry<String, List<Customer>> entrySet : customerPerCategory.entrySet()) {

			List<Customer> value = entrySet.getValue();
			if (value != null && !value.isEmpty()) {	
				
				Div divider = new Div();
				divider.add(new H5(entrySet.getKey()));
				divider.add(new Hr());

				outletNameComboBox.prependComponents(Iterables.get(value, 0), divider);
			}
			
		}
		outletNameComboBox.setLabel("Outlet/Store Name");
		outletNameComboBox.setWidth("50%");
		outletNameComboBox.setRequiredIndicatorVisible(true);
		outletNameComboBox.setItemLabelGenerator( e-> {
		 return e.getStoreName() + " (" + e.getOwnerName() + ")"; 
		});
		
		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");

		Button searchButton = new Button("Generate Report");
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.addClickListener(e-> {

			boolean isInvalid = false;
			if (outletNameComboBox.isEmpty()) {
				outletNameComboBox.setInvalid(true);
				outletNameComboBox.setRequiredIndicatorVisible(true);
				isInvalid = true;
			} else {
				outletNameComboBox.setInvalid(false);
				outletNameComboBox.setRequiredIndicatorVisible(false);
			}
			
			if (accountsReportType.isEmpty()) {
				accountsReportType.setInvalid(true);

				accountsReportType.setRequiredIndicatorVisible(true);
				isInvalid = true;
				

			} else {
				accountsReportType.setInvalid(false);

				accountsReportType.setRequiredIndicatorVisible(false);
			}
			
			if (AccountsReportType.STATEMENT_OF_ACCOUNT.equals(accountsReportType.getValue()) && periodType.isEmpty()) {
				periodType.setInvalid(true);
				periodType.setRequiredIndicatorVisible(true);
				isInvalid = true;
			} else {
				periodType.setInvalid(false);
				periodType.setRequiredIndicatorVisible(false);
			}
			
		
			
			if (isInvalid) {
				return;
			}
			
			reportContainer.removeAll();
			populateReport(reportContainer);
			

			
			
		});
		
		flexWrapper.add(searchButton);
		flexWrapper.setWidthFull();

		HorizontalLayout dateContainer = new HorizontalLayout();
		dateContainer.setVerticalComponentAlignment(Alignment.END, accountsReportType);
		dateContainer.setWidthFull();
		dateContainer.add(outletNameComboBox, accountsReportType);
		
		formContainer2.add(dateContainer, soaLayoutUserForms, soaLayout, datePicker, flexWrapper);
		
	}

	private void populateReport(VerticalLayout contentContainer) {
		



		AccountsReportType reportTypeValue =  accountsReportType.getValue();
		if (AccountsReportType.SUBSIDIARY_LEDGER == reportTypeValue) {
			
			Map<String, Object> filters = createOrderSearchCriteria();
			List<Order> orders = orderRepositoryCustom.filterBy(filters);
			

			setContent(contentContainer, orders);

		} else {

			Map<String, Object> filters = createSearchCriteria();
			Map<String, List<Payment>> payments = paymentRepositoryCustom.getPaymentsMapByFilterDates(filters);
			

			setContent(contentContainer, payments);
		}

	}
	
	private void setContent(VerticalLayout contentContainer, List<Order> orders) {
		AccountsReportType reportTypeValue =  accountsReportType.getValue();
		StreamResource resource = new StreamResource(reportTypeValue.getReportName() + ".csv", () -> {
			
			byte[] consolidatedReport = subsidiaryLedgerReport.buildReport(orders,outletNameComboBox.getValue()) ;
			return new ByteArrayInputStream(consolidatedReport);
		});
		
		addContentToViewer(resource,contentContainer);
		
	}

	private void addContentToViewer(StreamResource resource,VerticalLayout contentContainer) {
		PdfViewer pdfViewer = new PdfViewer();
		pdfViewer.setWidthFull();
		pdfViewer.setHeightFull();

		pdfViewer.setSrc(resource);
		pdfViewer.setAddDownloadButton(true);
		pdfViewer.setAddPrintButton(true);
		contentContainer.add(pdfViewer);
		contentContainer.setWidthFull();
		contentContainer.setHeight("100%");
		contentContainer.setVisible(true);
		
	}

	private Map<String, Object> createOrderSearchCriteria() {
		Map<String, Object> filters = Maps.newHashMap();
		
		LocalDate startDateFilter = null;
		LocalDate endDateFilter = null;
		
		AccountsReportType reportType = accountsReportType.getValue();
		if (AccountsReportType.STATEMENT_OF_ACCOUNT == reportType) {
			SoaPeriodType type = periodType.getValue();
			
			if (SoaPeriodType.CURRENT_MONTH == type) {
				startDateFilter = LocalDate.now().withDayOfMonth(1);
				endDateFilter = LocalDate.now();
			} else {
				startDateFilter = startDateSoa.getValue();
				endDateFilter = endDateSoa.getValue();
			}
		} else {
			if (!startDate.isEmpty() && !endDate.isEmpty()) {
				startDateFilter = startDate.getValue();
				endDateFilter = endDate.getValue();
			} 
		}
		

		
		Map<String, LocalDate> orderDates = Maps.newHashMap();
		orderDates.put("orderDateFrom", startDateFilter);
		orderDates.put("orderDateTo", endDateFilter);
		
		filters.put("ordersDate", orderDates);
		filters.put("store", Sets.newHashSet(outletNameComboBox.getValue()));



		
		return filters;
	}

	private Map<String, Object>  createSearchCriteria() {
		Map<String, Object> filters = Maps.newHashMap();
		
		LocalDate startDateFilter = null;
		LocalDate endDateFilter = null;
		
		AccountsReportType reportType = accountsReportType.getValue();
		if (AccountsReportType.STATEMENT_OF_ACCOUNT == reportType) {
			SoaPeriodType type = periodType.getValue();
			
			if (SoaPeriodType.CURRENT_MONTH == type) {
				startDateFilter = LocalDate.now().withDayOfMonth(1);
				endDateFilter = LocalDate.now();
			} else {
				startDateFilter = startDateSoa.getValue();
				endDateFilter = endDateSoa.getValue();
			}
		} else {
			if (!startDate.isEmpty() && !endDate.isEmpty()) {
				startDateFilter = startDate.getValue();
				endDateFilter = endDate.getValue();
			} 
		}
		

		
		Map<String, LocalDate> orderDates = Maps.newHashMap();
		orderDates.put("paymentDateFrom", startDateFilter);
		orderDates.put("paymentDateTo", endDateFilter);
		
		filters.put("paymentDateCriteria", orderDates);



		
		return filters;
		
	}

	private void setContent(VerticalLayout contentContainer, Map<String, List<Payment>> payments ) {
		
		AccountsReportType reportTypeValue =  accountsReportType.getValue();
		StreamResource resource = new StreamResource(reportTypeValue.getReportName() + ".csv", () -> {
			
			if (payments != null) {
				if (AccountsReportType.OUTSTANDING_CHEQUE_SUMMARY == reportTypeValue) {

					byte[] consolidatedReport = chequeSummaryReport.buildReport(payments, outletNameComboBox.getValue());
					return new ByteArrayInputStream(consolidatedReport);
				} else  if (AccountsReportType.ONLINE_PAYMENT_SUMMARY == reportTypeValue) {

					byte[] consolidatedReport = onlinePaymentSummaryReport.buildReport(payments, outletNameComboBox.getValue());
					return new ByteArrayInputStream(consolidatedReport);
				} 
			}
			return new ByteArrayInputStream(new byte[0]);
		});
		
		addContentToViewer(resource, contentContainer);
		
	}



	protected void createMainContentLayout(VerticalLayout mainContent) {
		
		
	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setHeightFull();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Accounts");
		header.addClassNames("mb-0", "mt-s", "text-xl");

		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub
		
	}

}

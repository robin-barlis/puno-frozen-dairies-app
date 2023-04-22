package com.example.application.views.reports;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.AccountsReportType;
import com.example.application.data.StatementPeriod;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.UserService;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.PaymentRepositoryCustom;
import com.example.application.data.service.payment.PaymentRepositoryCustomImpl;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.reports.RemittancesReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
	private VerticalLayout formContainer = new VerticalLayout();
	private AuthenticatedUser authenticatedUser;
	private RemittancesReport remittancesReport;
	private PaymentRepositoryCustom paymentRepositoryCustom;
	private CustomerService customerService;
	private List<Customer> customers;
	private ComboBox<AccountsReportType> accountsReportType;
	private ComboBox<Integer> yearPicker;
	private ComboBox<Month> monthPicker;

	private ComboBox<Integer> yearToPicker;
	private ComboBox<Month> monthToPicker;

	private ComboBox<Integer> yearSelectPicker;
	private ComboBox<Month> monthSelectPicker;
	private Map<ComboBox<Integer>, SelectedYearWrapper> selectedDateMap = Maps.newHashMap();
	private UserService userService;
	private Button addYearButton;
	private Button generateReportButton;
	private RadioButtonGroup<StatementPeriod> radioGroup;
	private FormLayout statementOfAccount;

	@Autowired
	public AccountsReportsView(OrdersService ordersService, PaymentsService paymentsService,
			PaymentRepositoryCustomImpl paymentRepositoryCustom, RemittancesReport remittancesReport,
			AuthenticatedUser authenticatedUser, CustomerService customerService, UserService userService) {
		super("products-view", "Remittances");
		this.paymentRepositoryCustom = paymentRepositoryCustom;
		this.remittancesReport = remittancesReport;
		this.authenticatedUser = authenticatedUser;
		this.customerService = customerService;
		this.userService = userService;
		addClassNames("administration-view");
		customers = customerService.listAll(Sort.unsorted());
		createMainContentLayout();
		populateReport(formContainer);

		mainDiv.add(formContainer);
		mainDiv.setHeightFull();

		add(mainDiv);
		

	}

	private void populateReport(VerticalLayout contentContainer) {

		statementOfAccount = createStatementOfAccountsForm();

		add(statementOfAccount);

		accountsReportType.setValue(AccountsReportType.STATEMENT_OF_ACCOUNT);
		accountsReportType.addValueChangeListener(e -> {

			if (AccountsReportType.STATEMENT_OF_ACCOUNT.equals(e.getValue())) {
				statementOfAccount.setVisible(true);
			}
		});

//		Map<String, Object> filters = createSearchCriteria();
//
//		Map<String, List<Payment>> payments = paymentRepositoryCustom.getPaymentsMapByFilterDates(filters);
//		
//
//		setContent(contentContainer, payments);

		// createStatementOfAccountsForm(contentContainer);
	}

	private FormLayout createStatementOfAccountsForm() {
		FormLayout form = new FormLayout();

		radioGroup = new RadioButtonGroup<>();
		radioGroup.addThemeVariants(RadioGroupVariant.MATERIAL_VERTICAL);

		radioGroup.setLabel("Specify Statement Account Period:");
		radioGroup.setItems(StatementPeriod.values());
		radioGroup.setValue(StatementPeriod.CURRENT_PERIOD);
		radioGroup.setWidthFull();

		LocalDate currentDate = LocalDate.now();

		radioGroup.setRenderer(new ComponentRenderer<>(periodType -> {
			String currentYearMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, getLocale())
					+ " " + currentDate.getYear();

			List<Integer> selectableYears = IntStream.range(2022, currentDate.getYear()).boxed()
					.collect(Collectors.toList());

			if (periodType == StatementPeriod.CURRENT_PERIOD) {
				VerticalLayout layout = new VerticalLayout();
				layout.add(new Span("Current Period - " + currentYearMonth));

				return layout;
			} else if (periodType == StatementPeriod.DATE_RANGE) {
				VerticalLayout layout = new VerticalLayout();
				yearPicker = new ComboBox<>();
				yearPicker.setItems(selectableYears);
				yearPicker.setPlaceholder("Select From Year");
				yearPicker.setEnabled(false);
				yearPicker.setValue(currentDate.getYear());

				monthPicker = new ComboBox<>();
				monthPicker.setItems(Month.values());
				monthPicker.setPlaceholder("Select From Month");
				monthPicker.setItemLabelGenerator(m -> m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
				monthPicker.setEnabled(false);
				monthPicker.setValue(currentDate.getMonth());

				yearToPicker = new ComboBox<>();
				yearToPicker.setItems(selectableYears);
				yearToPicker.setPlaceholder("Select From Year");
				yearToPicker.setEnabled(false);
				yearToPicker.setValue(currentDate.getYear());

				monthToPicker = new ComboBox<>();
				monthToPicker.setItems(Month.values());
				monthToPicker.setPlaceholder("Select From Month");
				monthToPicker.setItemLabelGenerator(m -> m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
				monthToPicker.setEnabled(false);
				monthToPicker.setValue(currentDate.getMonth());

				HorizontalLayout fromContainer = new HorizontalLayout();
				fromContainer.setVerticalComponentAlignment(Alignment.START, yearPicker, monthPicker);
				Span fromSpan = new Span("From ");
				fromSpan.setWidth("50px");
				fromContainer.add(fromSpan, yearPicker, monthPicker);

				HorizontalLayout toContainer = new HorizontalLayout();
				toContainer.setVerticalComponentAlignment(Alignment.START, yearToPicker, monthToPicker);
				Span toSpan = new Span("To ");
				toSpan.setWidth("50px");
				toContainer.add(toSpan, yearToPicker, monthToPicker);

				layout.add(fromContainer, toContainer);
				return layout;
			} else if (periodType == StatementPeriod.SELECTED_DATE) {
				VerticalLayout layout = new VerticalLayout();
				VerticalLayout toContainer = new VerticalLayout();
	
				SelectedYearWrapper newSelectedDate = new SelectedYearWrapper(selectableYears);
				toContainer.add(newSelectedDate);
				selectedDateMap.put(newSelectedDate.getYear(), newSelectedDate);
				Button removeButton = newSelectedDate.getRemoveButton();
				removeButton.addClickListener(event -> {
					toContainer.remove(newSelectedDate);

					selectedDateMap.remove(newSelectedDate.getYear());
				});
				
				
				
				addYearButton = new Button("Add Year");
				addYearButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
				addYearButton.setEnabled(false);
				addYearButton.addClickListener(event -> {
					

					SelectedYearWrapper selectePeriodWrapper = new SelectedYearWrapper(selectableYears);
					toContainer.add(selectePeriodWrapper);
					selectedDateMap.put(selectePeriodWrapper.getYear(), selectePeriodWrapper);
					selectePeriodWrapper.enableChildren();
					
					
					Button button = selectePeriodWrapper.getRemoveButton();
					button.addClickListener(ev -> {
						toContainer.remove(selectePeriodWrapper);

						selectedDateMap.remove(selectePeriodWrapper.getYear());
					});
					

				});

				HorizontalLayout buttonContainer = new HorizontalLayout();
				buttonContainer.add(addYearButton);

				Span selectedDateTitle = new Span("Select Statement Periods");
				layout.add(selectedDateTitle, toContainer, buttonContainer);
				return layout;
			}
			return new Icon(VaadinIcon.FEMALE);
		}));

		radioGroup.addValueChangeListener(radioGroupValue -> {
			
			if (StatementPeriod.DATE_RANGE.equals(radioGroup.getValue())) {

				yearPicker.setEnabled(true);
				monthPicker.setEnabled(true);
				yearToPicker.setEnabled(true);
				monthToPicker.setEnabled(true);

				selectedDateMap.values().forEach(SelectedYearWrapper::disableChildren);

				addYearButton.setEnabled(false);
			} else if (StatementPeriod.SELECTED_DATE.equals(radioGroup.getValue())) {

				yearPicker.setEnabled(false);
				monthPicker.setEnabled(false);
				yearToPicker.setEnabled(false);
				monthToPicker.setEnabled(false);
				selectedDateMap.values().forEach(SelectedYearWrapper::enableChildren);
				addYearButton.setEnabled(true);
			} else {

				yearPicker.setEnabled(false);
				monthPicker.setEnabled(false);
				yearToPicker.setEnabled(false);
				monthToPicker.setEnabled(false);
				selectedDateMap.values().forEach(SelectedYearWrapper::disableChildren);
				addYearButton.setEnabled(false);
			}
		});


		ComboBox<Customer> accountNameComboBox = new ComboBox<Customer>("Account Name");
		accountNameComboBox.setItems(customers);
		accountNameComboBox.setItemLabelGenerator(Customer::getOwnerName);
		accountNameComboBox.setWidth("50%");

		List<AppUser> users = userService.listAll(Sort.unsorted());

		ComboBox<AppUser> checkedBy = new ComboBox<AppUser>("Checked By");
		checkedBy.setItems(users);
		checkedBy.setItemLabelGenerator(appUser -> PfdiUtil.getFullName(appUser));
		checkedBy.setWidth("50%");

		HorizontalLayout fieldsLayout = new HorizontalLayout();
		fieldsLayout.setWidth("50%");

		ComboBox<AppUser> approvedBy = new ComboBox<AppUser>("Approved By");
		approvedBy.setItems(users);
		approvedBy.setItemLabelGenerator(appUser -> PfdiUtil.getFullName(appUser));
		approvedBy.setWidth("50%");

		fieldsLayout.add(checkedBy, approvedBy);

		form.add(accountNameComboBox, radioGroup, new Hr(), fieldsLayout);
		

		generateReportButton = new Button("Generate Report");
		generateReportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		generateReportButton.addClickListener(e -> {
			statementOfAccount.setVisible(false);
		});
		
		
		VerticalLayout generateButtonWrapper = new VerticalLayout();
		generateButtonWrapper.setJustifyContentMode(JustifyContentMode.END);
		generateButtonWrapper.add(new Hr(),generateReportButton);
		generateButtonWrapper.setPadding(false);

		generateButtonWrapper.setHorizontalComponentAlignment(Alignment.END, generateReportButton);
		form.add(generateButtonWrapper);
		form.setVisible(true);
		form.setResponsiveSteps(new ResponsiveStep("0", 1));

		return form;

	}

	private Map<String, Object> createSearchCriteria() {
		Map<String, Object> filters = Maps.newHashMap();

//		if (!datePickerFromDate.isEmpty() && !datePickerToDate.isEmpty()) {
//
//			Map<String, LocalDate> orderDates = Maps.newHashMap();
//			orderDates.put("paymentDateFrom", datePickerFromDate.getValue());
//			orderDates.put("paymentDateTo", !datePickerToDate.isEmpty() ? datePickerToDate.getValue() : LocalDate.now());
//
//			filters.put("paymentDateCriteria", orderDates);
//		} 

		return filters;

	}

	private void setContent(VerticalLayout contentContainer, Map<String, List<Payment>> payments) {

		StreamResource resource = new StreamResource("Daily-Remittances-Summary.pdf", () -> {

			if (payments != null) {

				byte[] consolidatedReport = remittancesReport.buildReport(payments, authenticatedUser.get().get());
				return new ByteArrayInputStream(consolidatedReport);
			}
			return new ByteArrayInputStream(new byte[0]);
		});

		PdfViewer pdfViewer = new PdfViewer();
		pdfViewer.setWidthFull();
		pdfViewer.setHeightFull();

		pdfViewer.setSrc(resource);
		pdfViewer.setAddDownloadButton(true);
		pdfViewer.setAddPrintButton(true);
		contentContainer.add(pdfViewer);
		contentContainer.setWidthFull();
		contentContainer.setHeight("75%");

	}

	protected void createMainContentLayout() {
		HorizontalLayout optionsContainer = new HorizontalLayout();
		optionsContainer.setJustifyContentMode(JustifyContentMode.END);
		optionsContainer.setPadding(false);
		optionsContainer.setWidth("100%");

		VerticalLayout filterLayout = new VerticalLayout();

		accountsReportType = new ComboBox<AccountsReportType>("Report Type");
		accountsReportType.setItems(Lists.newArrayList(AccountsReportType.values()));
		accountsReportType.setWidth("50%");
		accountsReportType.setItemLabelGenerator(AccountsReportType::getReportName);

		FormLayout dateContainer = new FormLayout();
		dateContainer.setWidthFull();
		dateContainer.add(accountsReportType);
		
		filterLayout.setPadding(false);
		filterLayout.add(dateContainer);
		filterLayout.add(new Hr());
		add(filterLayout);
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

	private class SelectedYearWrapper extends HorizontalLayout {

		private static final long serialVersionUID = 1L;

		Button removeButton;

		private ComboBox<Integer> yearSelectPicker;
		private MultiSelectComboBox<Month> monthSelectPicker;

		protected SelectedYearWrapper(List<Integer> selectableYears) {

			LocalDate currentDate = LocalDate.now();
			yearSelectPicker = new ComboBox<>();
			yearSelectPicker.setItems(selectableYears);
			yearSelectPicker.setPlaceholder("Select Year");
			yearSelectPicker.setEnabled(false);
			yearSelectPicker.setValue(currentDate.getYear());

			monthSelectPicker = new MultiSelectComboBox<>();
			monthSelectPicker.setItems(Month.values());
			monthSelectPicker.setPlaceholder("Select Month");
			monthSelectPicker.setItemLabelGenerator(m -> m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
			monthSelectPicker.setEnabled(false);
			monthSelectPicker.setValue(currentDate.getMonth());
			
			
			removeButton = new Button(new Icon(VaadinIcon.MINUS));
			removeButton.setEnabled(false);
			
			add(yearSelectPicker, monthSelectPicker, removeButton);


		}

		public void enableChildren() {
			setEnabledChildren(true);
			
		}
		
		public void disableChildren() {
			setEnabledChildren(false);
			
		}

		public ComboBox<Integer> getYear() {
			return yearSelectPicker;
		}

		public MultiSelectComboBox<Month> getMonth() {
			return monthSelectPicker;
		}

		public Button getRemoveButton() {
			return removeButton;
		}


		private void setEnabledChildren(boolean enabled) {
			removeButton.setEnabled(enabled);
			yearSelectPicker.setEnabled(enabled);
			monthSelectPicker.setEnabled(enabled);
			
		}
	}
}

package com.example.application.views.reports;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.AccountsReportType;
import com.example.application.data.service.UserService;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.PaymentRepositoryCustomImpl;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.reports.RemittancesReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.reports.forms.StatementOfAccountForm;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Stock Orders")
@Route(value = "reports/accounts2/", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class AccountsReportsView2 extends AbstractPfdiView {

	private static final long serialVersionUID = 2754507440441771890L;
	private VerticalLayout mainDiv = new VerticalLayout();
	private VerticalLayout formContainer = new VerticalLayout();

	private ComboBox<AccountsReportType> accountsReportType;
	private FormLayout statementOfAccount;

	private CustomerService customerService;

	private UserService userService;

	private Button generateReportButton;

	@Autowired
	public AccountsReportsView2(OrdersService ordersService, PaymentsService paymentsService,
			PaymentRepositoryCustomImpl paymentRepositoryCustom, RemittancesReport remittancesReport,
			AuthenticatedUser authenticatedUser, CustomerService customerService, UserService userService) {
		super("products-view", "Remittances");
		this.customerService = customerService;

		this.userService = userService;
		addClassNames("administration-view");
		//customers = customerService.listAll(Sort.unsorted());
		createMainContentLayout();
		populateReport(formContainer);
		
		

		generateReportButton = new Button("Generate Report");
		generateReportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		generateReportButton.addClickListener(e -> {
			
		});
		
		VerticalLayout generateButtonWrapper = new VerticalLayout();
		generateButtonWrapper.setJustifyContentMode(JustifyContentMode.END);
		generateButtonWrapper.add(new Hr(), generateReportButton);
		generateButtonWrapper.setPadding(false);

		generateButtonWrapper.setHorizontalComponentAlignment(Alignment.END, generateReportButton);
	

		mainDiv.add(formContainer);
		mainDiv.add(generateButtonWrapper);
		mainDiv.setHeightFull();

		add(mainDiv);
		

	}

	private void populateReport(VerticalLayout contentContainer) {

		createStatementOfAccountsForm();
		
		accountsReportType.setValue(AccountsReportType.STATEMENT_OF_ACCOUNT);
		accountsReportType.addValueChangeListener(e -> {

			if (AccountsReportType.STATEMENT_OF_ACCOUNT.equals(e.getValue())) {
				statementOfAccount.setVisible(true);
			}
		});
	}

	private void createStatementOfAccountsForm() {
		
		StatementOfAccountForm soaForm = new StatementOfAccountForm(customerService, userService);
		soaForm.setVisible(true);
		add(soaForm);

	}

//	private Map<String, Object> createSearchCriteria() {
//		Map<String, Object> filters = Maps.newHashMap();
//
//		return filters;
//
//	}
//
//	private void setContent(VerticalLayout contentContainer, Map<String, List<Payment>> payments) {
//
//		StreamResource resource = new StreamResource("Daily-Remittances-Summary.pdf", () -> {
//
//			if (payments != null) {
//
//				byte[] consolidatedReport = remittancesReport.buildReport(payments, authenticatedUser.get().get());
//				return new ByteArrayInputStream(consolidatedReport);
//			}
//			return new ByteArrayInputStream(new byte[0]);
//		});
//
//		PdfViewer pdfViewer = new PdfViewer();
//		pdfViewer.setWidthFull();
//		pdfViewer.setHeightFull();
//
//		pdfViewer.setSrc(resource);
//		pdfViewer.setAddDownloadButton(true);
//		pdfViewer.setAddPrintButton(true);
//		contentContainer.add(pdfViewer);
//		contentContainer.setWidthFull();
//		contentContainer.setHeight("75%");
//
//	}

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
}

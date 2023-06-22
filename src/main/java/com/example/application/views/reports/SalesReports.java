package com.example.application.views.reports;

import java.io.ByteArrayInputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.orders.OrderRepositoryCustom;
import com.example.application.data.service.orders.OrderRepositoryCustomImpl;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.PaymentRepositoryCustom;
import com.example.application.data.service.payment.PaymentRepositoryCustomImpl;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.reports.RemittancesReport;
import com.example.application.reports.SalesReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.google.common.collect.Maps;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H1;
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
import com.vaadin.flow.server.StreamResource;

@PageTitle("Stock Orders")
@Route(value = "reports/sales/", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class SalesReports extends AbstractPfdiView {


	private static final long serialVersionUID = 2754507440441771890L;

	private byte[] stockTransferData;

	private VerticalLayout mainDiv = new VerticalLayout();
	private VerticalLayout reportContainer = new VerticalLayout();
	private AuthenticatedUser authenticatedUser;
    private SalesReport salesReport;
	private OrderRepositoryCustom orderRepositoryCustom;

    private DatePicker datePickerFromDate;
    private DatePicker datePickerToDate;



	@Autowired
	public SalesReports(OrdersService ordersService, 
			PaymentsService paymentsService, 
			OrderRepositoryCustomImpl orderRepositoryCustom,
			SalesReport salesReport,
			AuthenticatedUser authenticatedUser) {
		super("products-view", "Sales");
		this.orderRepositoryCustom = orderRepositoryCustom;
		this.salesReport = salesReport;
		this.authenticatedUser = authenticatedUser;
		addClassNames("administration-view");

		populateReport(reportContainer);
		mainDiv.add(reportContainer);
		mainDiv.setHeightFull();

		add(mainDiv);
	}
	
	private void populateReport(VerticalLayout contentContainer) {


		Map<String, Object> filters = createSearchCriteria();

		List<Order> orders = orderRepositoryCustom.filterBy(filters);
		

		setContent(contentContainer, orders);
	}
	
	private Map<String, Object>  createSearchCriteria() {
		Map<String, Object> filters = Maps.newHashMap();


		if (!datePickerFromDate.isEmpty() && !datePickerToDate.isEmpty()) {
			

			Map<String, LocalDate> orderDates = Maps.newHashMap();
			orderDates.put("orderDateFrom", datePickerFromDate.getValue());
			orderDates.put("orderDateTo", !datePickerToDate.isEmpty() ? datePickerToDate.getValue() : LocalDate.now());

			filters.put("ordersDate", orderDates);
		} 
		
		return filters;
		
	}

	private void setContent(VerticalLayout contentContainer, List<Order> orders ) {
		
    	
		StreamResource resource = new StreamResource("STOCK_ORDER_SUMMARY.pdf", () -> {

			if (orders != null) {

				byte[] consolidatedReport = salesReport.buildReport(orders);
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



	protected void createMainContentLayout(VerticalLayout mainContent) {
		HorizontalLayout optionsContainer = new HorizontalLayout();
		optionsContainer.setJustifyContentMode(JustifyContentMode.END);
		optionsContainer.setPadding(false);
		optionsContainer.setWidth("100%");

		VerticalLayout filterLayout = new VerticalLayout();
		
		DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
		DayOfWeek lastDayOfWeek = DayOfWeek.of(((firstDayOfWeek.getValue() + 5) % DayOfWeek.values().length) + 1);

		LocalDate currentFrom = LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek)); // first day
		LocalDate currentTo =LocalDate.now().with(TemporalAdjusters.nextOrSame(lastDayOfWeek)); 
		
		datePickerFromDate = new DatePicker("Date From:");
		datePickerFromDate.setValue(currentFrom);
		datePickerToDate = new DatePicker("Date To:");
		datePickerToDate.setValue(currentTo);
		
		Button searchButton = new Button("Generate Report");
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.addClickListener(e-> {
			
			reportContainer.removeAll();
			populateReport(reportContainer);
			
			
		});
		
		HorizontalLayout dateContainer = new HorizontalLayout();
		dateContainer.setVerticalComponentAlignment(Alignment.END, searchButton);
		dateContainer.add(datePickerFromDate, datePickerToDate, searchButton);
		
		
		
		filterLayout.add(dateContainer);
		
		
		

		mainContent.add(filterLayout);
		
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
		H1 header = new H1("Remittances");
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

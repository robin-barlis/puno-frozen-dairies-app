package com.example.application.views.reports;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.entity.payment.Payment;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.payment.PaymentRepositoryCustom;
import com.example.application.data.service.payment.PaymentRepositoryCustomImpl;
import com.example.application.data.service.payment.PaymentsService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.google.common.collect.Maps;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Reports")
@Route(value = "reports/stocks", layout = MainLayout.class)
@PermitAll
public class StocksReports extends AbstractPfdiView implements HasComponents, HasStyle {

	private static final long serialVersionUID = -6210105239749320428L;


	private OrdersService ordersService;
	private PaymentsService paymentsService;
	private PaymentRepositoryCustom paymentRepositoryCustom;
	
    private Tab all;
    private Tab cash;
    private Tab bank;
    private Tab cheque;
    DatePicker datePickerFromDate;
    DatePicker datePickerToDate;
    
    Grid<Payment> cashPaymentsGrid; 
    Grid<Payment> bankRemittancePaymentsGrid; 
    Grid<Payment> chequePaymentsGrid; 


	@Autowired
	public StocksReports(OrdersService ordersService, PaymentsService paymentsService, PaymentRepositoryCustomImpl paymentRepositoryCustom) {
		super("products-view", "Reports");
		this.ordersService = ordersService;
		this.paymentRepositoryCustom = paymentRepositoryCustom;
		populateGrids();
	}

	private void populateGrids() {


		Map<String, Object> filters = createSearchCriteria();

		Map<String, List<Payment>> thisWeeksPayments = paymentRepositoryCustom.getPaymentsMapByFilterDates(filters);
		
		
		
		
		
		
	}

	@Override
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Remittances");
		header.addClassNames("mb-0", "mt-s", "text-xl");

		// headerNameWrapper.add(tabs);
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
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
		

		
		HorizontalLayout dateContainer = new HorizontalLayout();
		dateContainer.add(datePickerFromDate, datePickerToDate);

		all = new Tab("All");
		cash = new Tab("Cash Payments");
		bank = new Tab("Bank Remittances");
		cheque = new Tab("Cheques");
		
		VerticalLayout contentContainer = new VerticalLayout();
		
	    Tabs tabs = new Tabs();
	   
	    tabs.add(all, cash, bank, cheque);

	    tabs.addSelectedChangeListener(e -> setContent(e.getSelectedTab(), contentContainer));
	    tabs.setSelectedTab(all);
		
		HorizontalLayout filterButtonsContainer = new HorizontalLayout();
		filterButtonsContainer.add(tabs);
		
		setContent(all, contentContainer);
		
		filterLayout.add(dateContainer, filterButtonsContainer);
		

		mainContent.add(filterLayout, contentContainer);
		
		
	}

	private Map<String, Object>  createSearchCriteria() {
		Map<String, Object> filters = Maps.newHashMap();


		if (!datePickerFromDate.isEmpty() && !datePickerToDate.isEmpty()) {

			Map<String, LocalDate> orderDates = Maps.newHashMap();
			orderDates.put("paymentDateFrom", datePickerFromDate.getValue());
			orderDates.put("paymentDateTo", !datePickerToDate.isEmpty() ? datePickerToDate.getValue() : LocalDate.now());

			filters.put("paymentDateCriteria", orderDates);
		} 
		
		return filters;
		
	}

	private void setContent(Tab selectedTab, VerticalLayout contentContainer) {
		contentContainer.removeAll();
	        if (selectedTab == null) {
	            return;
	        }
	        if (selectedTab.equals(all)) {
	        	contentContainer.add(new Paragraph("This is the All tab"));
	        } else if (selectedTab.equals(cash)) {
	        	contentContainer.add(new Paragraph("This is the Cash tab"));
	        } else if (selectedTab.equals(bank)) {
	        	contentContainer.add(new Paragraph("This is the Bank tab"));
	        } else if (selectedTab.equals(cheque)){
	        	contentContainer.add(new Paragraph("This is the Cheque tab"));
	        }
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub
		
	}


}
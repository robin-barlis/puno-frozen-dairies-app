package com.example.application.views.order;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.Reports;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.data.service.products.SizesService;
import com.example.application.reports.DeliveryReceiptReport;
import com.example.application.reports.OrderSummaryReport;
import com.example.application.reports.SalesInvoiceReport;
import com.example.application.reports.StockTransferReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.utils.service.ReportConsolidatorService;
import com.example.application.views.MainLayout;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
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
import com.vaadin.flow.server.StreamResource;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;

@PageTitle("Stock Orders")
@Route(value = "order/printAll/:id/:report", layout = MainLayout.class)
@RouteAlias(value = "/order/printAll/:id/:report", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class PrinterView extends VerticalLayout implements BeforeEnterObserver {


	private static final long serialVersionUID = 2754507440441771890L;
	
	private Button back;

	private OrdersService ordersService;
	
	
	
	private String orderId;
	private String report;
//	private BigDecimal totalAmount = BigDecimal.valueOf(0);

	private AppUser appUser;
	private SizesService sizesService;
	private OrderSummaryReport orderSummaryReport;
	private DeliveryReceiptReport deliveryReceiptReport;
	private SalesInvoiceReport salesInvoiceReport;
	private StockTransferReport stockTransferReport;
	
//
//	private byte[] orderSummaryReportData;
	private Div mainDiv = new Div();

	private ReportConsolidatorService reportConsolidatorService;


	@Autowired
	public PrinterView(SizesService sizesService, OrdersService ordersService, AuthenticatedUser user, 
			OrderSummaryReport orderSummaryReport, 
			ReportConsolidatorService reportConsolidatorService,
			DeliveryReceiptReport deliveryReceiptReport,
			SalesInvoiceReport salesInvoiceReport,
			StockTransferReport stockTransferReport) {
		this.ordersService = ordersService;
		this.appUser = user.get().get();
		
		this.sizesService = sizesService;
		this.orderSummaryReport = orderSummaryReport;
		this.reportConsolidatorService = reportConsolidatorService;
		this.deliveryReceiptReport = deliveryReceiptReport;
		this.stockTransferReport = stockTransferReport;
		this.salesInvoiceReport = salesInvoiceReport;
		addClassNames("administration-view");
		
		addChildrenToContentHeaderContainer(this);
		
		Div actionButtonDiv = new Div();
		actionButtonDiv.addClassName("action-button-wrapper");
		MenuBar menuBar = new MenuBar();
		menuBar.addClassName("float-right");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);


        
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
		
	
		
		buttonContainer.add(back);
		
		add(buttonContainer);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		orderId = event.getRouteParameters().get("id").get();
		report = event.getRouteParameters().get("report").get();
		
		List<Order> orders = Lists.newArrayList();
		
		for (String string : orderId.split(",")) {

			Order order = ordersService.get(Integer.parseInt(string)).get();
			orders.add(order);
		}
		
		
		Collections.sort(orders, (o1, o2) -> {
			return o1.getStockOrderNumber().compareTo(o2.getStockOrderNumber());
		});
    	List<JasperReportBuilder> orderSummaryReportBuilder = Lists.newArrayList();
    	
    	
    	for (Order order : orders) {
    		
    		if (report.equals(Reports.ALL.name()) || report.equals(Reports.SO.name()) ) {
    			JasperReportBuilder orderReport = createStockOrderReports(Reports.SO, 
    					order, 
    					sizesService.listAll(Sort.unsorted()), appUser);
    			
    			if (orderReport != null) {

            		orderSummaryReportBuilder.add(orderReport);
    			}
    		}
    		
    		if ((report.equals(Reports.ALL.name()) || report.equals(Reports.SI.name())) && !PfdiUtil.isRelativeOrCompanyOwned(order.getCustomer().getCustomerTagId())) {
    			JasperReportBuilder orderReport = createStockOrderReports(Reports.SI, 
    					order, 
    					sizesService.listAll(Sort.unsorted()), appUser);

    			if (orderReport != null) {

            		orderSummaryReportBuilder.add(orderReport);
    			}
    		}
    		
    		if (report.equals(Reports.ALL.name()) || report.equals(Reports.ST.name()) ) {
    			JasperReportBuilder orderReport = createStockOrderReports(Reports.ST, 
    					order, 
    					sizesService.listAll(Sort.unsorted()), appUser);

    			if (orderReport != null) {

            		orderSummaryReportBuilder.add(orderReport);
    			}
    		}
    		
    		if (report.equals(Reports.ALL.name()) || report.equals(Reports.DR.name()) ) {
    			JasperReportBuilder orderReport = createStockOrderReports(Reports.DR, 
    					order, 
    					sizesService.listAll(Sort.unsorted()), appUser);

    			if (orderReport != null) {

            		orderSummaryReportBuilder.add(orderReport);
    			}
    		}
    		
    	}
    	
    	byte[] consolidatedReport = reportConsolidatorService.build(orderSummaryReportBuilder.toArray(new JasperReportBuilder[orderSummaryReportBuilder.size()]));
    	
		StreamResource resource = new StreamResource("order.pdf", () -> {
			if (orders != null) {
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
		mainDiv.add(pdfViewer);
		mainDiv.setWidthFull();
		mainDiv.setHeight("75%");
	}

	private JasperReportBuilder createStockOrderReports(Reports report, Order order, List<Size> listAll, AppUser appUser2) {
		JasperReportBuilder reportBuilder = null;
		
		if (Reports.SO == report) {
			reportBuilder = orderSummaryReport
					.getReportBuilder(order, 
							sizesService.listAll(Sort.unsorted()), 
							appUser);
			
		} else if (Reports.SI == report) {
			reportBuilder = salesInvoiceReport
					.getReportBuilder(order);
			
		} else if (Reports.ST == report) {
			reportBuilder = stockTransferReport
					.getReportBuilder(order);
			
		} else if (Reports.DR == report) {
			reportBuilder = deliveryReceiptReport
					.getReportBuilder(order);
			
		}

		return reportBuilder;
		

		
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

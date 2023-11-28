package com.example.application.views.order;

import java.io.ByteArrayInputStream;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.entity.orders.Order;
import com.example.application.data.service.orders.DocumentTrackingNumberService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.reports.StockTransferReport;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.MainLayout;
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

@PageTitle("Stock Orders")
@Route(value = "order/stockTransfer/:id", layout = MainLayout.class)
@RouteAlias(value = "/order/stockTransfer/:id", layout = MainLayout.class)
@RolesAllowed({ "Superuser", "Checker", "Sales", "CHECKER", "SALES" })
@Uses(Icon.class)
public class StockTransferView extends VerticalLayout implements BeforeEnterObserver {


	private static final long serialVersionUID = 2754507440441771890L;

	private Button back;

	private OrdersService ordersService;

	private Order order;

	private String orderId;

	private byte[] stockTransferData;

	private StockTransferReport stockTransferReport;
	private Div mainDiv = new Div();



	@Autowired
	public StockTransferView(OrdersService ordersService, AuthenticatedUser user,
			DocumentTrackingNumberService documentTrackingNumberService, StockTransferReport stockTransferReport) {
		this.ordersService = ordersService;
		this.stockTransferReport = stockTransferReport;
		addClassNames("administration-view");

		addChildrenToContentHeaderContainer(this);

		Div actionButtonDiv = new Div();
		actionButtonDiv.addClassName("action-button-wrapper");
		MenuBar menuBar = new MenuBar();
		menuBar.addClassName("float-right");
		menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

		actionButtonDiv.add(menuBar);
		add(actionButtonDiv);

		mainDiv.addClassName("order-summary-div");

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

		order = ordersService.get(Integer.parseInt(orderId)).get();

		
		Integer stockOrderNumber = order.getStockOrderNumber();
		if (stockOrderNumber == null) {
			stockOrderNumber = generateStockOrderNumber();
			order.setStockOrderNumber(stockOrderNumber);
		}


		try {
			stockTransferData = stockTransferReport.buildReport(order);
		} catch (Exception e ) {

			e.printStackTrace();
		}
		PdfViewer pdfViewer = new PdfViewer();
		pdfViewer.setWidthFull();
		pdfViewer.setHeightFull();
		StreamResource resource = new StreamResource("order.pdf", () -> {
			if (order != null) {
				return new ByteArrayInputStream(stockTransferData);
			}
			return new ByteArrayInputStream(new byte[0]);
		});
		pdfViewer.setSrc(resource);
		pdfViewer.setAddPrintButton(true);
		mainDiv.add(pdfViewer);
		mainDiv.setWidthFull();
		mainDiv.setHeight("75%");
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

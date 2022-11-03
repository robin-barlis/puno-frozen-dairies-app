package com.example.application.views.order;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.customers.Customer;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.AddNewProductView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrders", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrders", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class StockOrderView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<Order> grid = new Grid<>(Order.class, false); Button addCustomerButton;
	
	private OrdersService ordersService;
	private final CustomerService customerService;

	private ListDataProvider<Order> ldp = null;

	@Autowired
	public StockOrderView(OrdersService ordersService, CustomerService customerService) {
		super("Admin", "Admin");
		this.customerService = customerService;
		this.ordersService = ordersService;
		addClassNames("administration-view");

		VerticalLayout tableContent = new VerticalLayout();
		createGridLayout(tableContent);

		add(tableContent);

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

		addCustomerButton = new Button("Create Stock Order");
		addCustomerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addCustomerButton.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
	
		addCustomerButton.addClickListener(e -> {

			UI.getCurrent().navigate(CreateOrderFormView.class);
		});
		flexWrapper.add(addCustomerButton);
		flexWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		List<Customer> customers = customerService.listAll(Sort.by("id"));

		if (!customers.isEmpty()) {
			//customers.forEach(customer -> populateForm(customer));

		} else {
			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
					Notification.Position.BOTTOM_START);
			refreshGrid();
			event.forwardTo(StockOrderView.class);
		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		
		grid.addColumn(order -> {			
			return order.getId();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Stock Order Number").setSortable(true);
		
		grid.addColumn(order -> {			
			return order.getCustomer().getStoreName();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Store Name").setSortable(true);
		
		grid.addColumn(order -> {			
			return order.getCreatedByUser().getUsername();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Created By").setSortable(true);
		
		grid.addColumn(order -> {			
			return order.getCreationDate();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Created Date").setSortable(true);
		
		grid.addColumn(order -> {	
			
			if (order.getCheckedByUser() != null) {

				return order.getCheckedByUser().getUsername();
			} else {
				return StringUtils.EMPTY;
			}
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Checked By").setSortable(true);
		
		grid.addColumn(order -> {			
			return order.getCheckedDate();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Checked Date").setSortable(true);
		

		grid.addColumn("status").setAutoWidth(true).setTextAlign(ColumnTextAlign.START);

		

		ldp = DataProvider.ofCollection(ordersService.listAll(Sort.by("id")));

		GridListDataView<Order> dataView = grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		TextField searchField = new TextField();
		searchField.setPlaceholder("Search by owner name or store name");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
		searchField.setSuffixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> dataView.refreshAll());
		searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);

		dataView.addFilter(customer -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty())
				return true;

//			boolean matchesCustomerName = matchesTerm(customer.getStoreName(), searchTerm);
//			boolean matchesOwnerName = matchesTerm(customer.getOwnerName(), searchTerm);

			//return matchesCustomerName || matchesOwnerName;
			return true;
		});

		wrapper.add(searchField, grid);
		verticalLayout.addAndExpand(wrapper);
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getListDataView().refreshAll();
	}
//
//	private void refreshGrid(Order updatedOrder, boolean delete) {
//		if (delete) {
//
//			grid.getListDataView().removeItem(updatedOrder);
//		} else {
//
//			grid.getListDataView().addItem(updatedOrder);
//		}
//		ldp.refreshItem(updatedOrder);
//		refreshGrid();
//
//	}

//	private boolean matchesTerm(String value, String searchTerm) {
//		return value.toLowerCase().contains(searchTerm.toLowerCase());
//	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

}

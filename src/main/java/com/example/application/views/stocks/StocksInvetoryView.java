package com.example.application.views.stocks;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.stock.ItemStock;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Inventory")
@Route(value = "stocks/inventory", layout = MainLayout.class)
@RouteAlias(value = "stocks/inventory", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN", "Checker", "CHECKER", "Sales", "SALES" })
@Uses(Icon.class)
public class StocksInvetoryView extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Grid<ItemStock> grid = new Grid<>(ItemStock.class, false);

	private ListDataProvider<ItemStock> ldp = null;
	private ItemStockService itemStockService;


	@Autowired
	public StocksInvetoryView(ItemStockService itemStockService) {
		super("Admin", "Admin");
		addClassNames("administration-view");
		this.itemStockService = itemStockService;

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
		H1 header = new H1("Inventory");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		headerContainer.add(headerNameWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

//		List<Customer> customers = customerService.listAll(Sort.by("id"));
//
//		if (!customers.isEmpty()) {
//			customers.forEach(customer -> populateForm(customer));
//
//		} else {
//			Notification.show(String.format("No customers available. Please contact your administrator."), 3000,
//					Notification.Position.BOTTOM_START);
//			refreshGrid();
//			event.forwardTo(StocksInvetoryView.class);
//		}
	}

	private void createGridLayout(VerticalLayout verticalLayout) {

		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		
		grid.addColumn(itemStock -> {	
			
			
			return itemStock.getProduct().getCategory().getCategoryName();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Category").setSortable(true);
		
		grid.addColumn(itemStock -> {			
			return itemStock.getProduct().getProductName();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Name").setSortable(true);
		
		grid.addColumn(itemStock -> {			
			return itemStock.getSize().getSizeName();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Size").setSortable(true);
		

		grid.addColumn("availableStock").setHeader("Available").setTextAlign(ColumnTextAlign.START);

		
		
		
		grid.addComponentColumn(itemStock -> {
			
			
			
			
			HorizontalLayout adjustmentFieldSaveWrapper = new HorizontalLayout();
			IntegerField adjustmentField = new IntegerField();
			//adjustmentField.setValue(0);
			adjustmentField.setHasControls(true);
			
			Button saveButton = new Button(new Icon(VaadinIcon.PLUS)); // make this an icon
			saveButton.setVisible(false);
			saveButton.addClickListener(e -> {
				int currentStock = itemStock.getAvailableStock();
				itemStock.setAvailableStock(currentStock + adjustmentField.getValue());
				
				itemStockService.update(itemStock);
				ldp.refreshItem(itemStock);
				//grid.getListDataView().refreshAll();
				Notification.show("Successfully adjusted available stocks for " + itemStock.getProduct().getProductName() + " (" + itemStock.getSize().getSizeName() + ")")
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			});
			saveButton.addThemeVariants(ButtonVariant.LUMO_ICON);
			saveButton.getElement().setAttribute("aria-label", "Save Adjustment");
			
			Button cancelButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
			cancelButton.setVisible(false);
			cancelButton.addClickListener(e -> {
				adjustmentField.setValue(0);
				saveButton.setVisible(false);
				cancelButton.setVisible(false);
			});
			cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON);
			cancelButton.getElement().setAttribute("aria-label", "Cancel Adjustment");
			

			adjustmentFieldSaveWrapper.add(adjustmentField, saveButton, cancelButton);
			
			adjustmentField.addValueChangeListener(e -> {
				if (!saveButton.isVisible()) {
					saveButton.setVisible(true);
				}
				if (!cancelButton.isVisible()) {
					cancelButton.setVisible(true);
				}
			
			});
		    
		    return adjustmentFieldSaveWrapper; 
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Adjustment").setSortable(true);
		
		
		ldp = DataProvider.ofCollection(itemStockService.listAll(Sort.by("id")));

		GridListDataView<ItemStock> dataView = grid.setItems(ldp);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
		grid.scrollIntoView();

		TextField searchField = new TextField();
		searchField.setPlaceholder("Search by product, size or category");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
		searchField.setSuffixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> dataView.refreshAll());
		searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);

		dataView.addFilter(itemStock -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty())
				return true;

			boolean matchesCustomerName = matchesTerm(itemStock.getProduct().getProductName(), searchTerm);
			boolean matchesOwnerName = matchesTerm(itemStock.getSize().getSizeName(), searchTerm);
			boolean matchesCategory = matchesTerm(itemStock.getProduct().getCategory().getCategoryName(), searchTerm);

			return matchesCustomerName || matchesOwnerName || matchesCategory;
		});

		wrapper.add(searchField, grid);
		verticalLayout.addAndExpand(wrapper);
	}


	private boolean matchesTerm(String value, String searchTerm) {
		return value.toLowerCase().contains(searchTerm.toLowerCase());
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}

}

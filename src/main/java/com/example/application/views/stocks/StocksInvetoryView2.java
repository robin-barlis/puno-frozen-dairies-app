package com.example.application.views.stocks;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.data.entity.stock.Inventory;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.constants.CssClassNamesConstants;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
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
public class StocksInvetoryView2 extends AbstractPfdiView implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private TreeGrid<Inventory> grid;

	private ItemStockService itemStockService;


	@Autowired
	public StocksInvetoryView2(ItemStockService itemStockService, ProductService productService) {
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
		
		FlexLayout flexWrapper = new FlexLayout();
		flexWrapper.setFlexDirection(FlexDirection.ROW);
		flexWrapper.setJustifyContentMode(JustifyContentMode.END);
		flexWrapper.setClassName("button-layout");
		flexWrapper.setWidth("50%");
		
		Button manageInventory = new Button("Manage Inventory");
		manageInventory.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		manageInventory.setClassName(CssClassNamesConstants.GENERIC_BUTTON_CLASS);
	
		manageInventory.addClickListener(e -> {
			UI.getCurrent().navigate(ManageInventoryView.class);
		});

		flexWrapper.add(manageInventory);
		headerContainer.add(headerNameWrapper, flexWrapper);
		contentHeaderContainer.add(headerContainer);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {


	}

	private void createGridLayout(VerticalLayout verticalLayout) {
		

		List<Inventory> items = itemStockService.listAllByProduct(null);
		

		grid = new TreeGrid<>();
		grid.addHierarchyColumn(itemStock -> {	
			return itemStock.getName();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Item").setSortable(true);
		
		grid.addColumn(itemStock -> {			
			return itemStock.getCategory();
		}).setAutoWidth(true).setTextAlign(ColumnTextAlign.START).setHeader("Category").setSortable(true);
		
		grid.addColumn(itemStock -> {
			return itemStock.getQuantity();
		}).setHeader("On Hand Stock").setTextAlign(ColumnTextAlign.START);

		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);

		

		TextField searchField = new TextField();
		searchField.setPlaceholder("Search by product, size or category");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.setClassName(CssClassNamesConstants.PFDI_ICONS);
		searchField.setSuffixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addClassName(CssClassNamesConstants.SEARCH_FILTER_FIELD);
		
		grid.setItems(items, this::getSubInventory);
		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");

		wrapper.add(searchField, grid);
		verticalLayout.addAndExpand(wrapper);
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContentContainer) {

	}
	
	public List<Inventory> getSubInventory(Inventory inventory) {
        return inventory.getSubinventory();
    }

}

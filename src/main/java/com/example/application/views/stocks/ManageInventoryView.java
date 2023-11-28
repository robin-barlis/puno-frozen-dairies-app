package com.example.application.views.stocks;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.stock.ItemStock;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Products")
@Route(value = "stocks/inventory/manage", layout = MainLayout.class)
@RouteAlias(value = "stocks/inventory/manage", layout = MainLayout.class)
@PermitAll
public class ManageInventoryView extends AbstractPfdiView implements HasStyle {

	private static final long serialVersionUID = -6210105239749320428L;

	private TextField productName;
	private TextField shortCode;
	private MultiSelectComboBox<Product> product;
	private Button cancelButton;
	//private Button saveAsDraftButton;
	private Button publishButton;
	private HashSet<ProductInventorySubView> productInventorySubViewSet = new HashSet<>();
	private ItemStockService itemStockService;
	List<Category> categories;
	List<Product> products;
	Set<ProductPrice> productPrice= null;
	File file = null;
	private AppUser user;

	@Autowired
	public ManageInventoryView(AuthenticatedUser authenticatedUser, CategoryService categoryService, ProductService productService, ItemStockService itemStockService) {
		super("add-new-product", "Manage Inventory");
		addClassNames("products-view");
		this.itemStockService = itemStockService;
		this.products = productService.listAll(Sort.unsorted());
		this.user = authenticatedUser.get().get();

		this.categories = categoryService.listAll(Sort.by("id"));
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}

	private void createMainContent(VerticalLayout content) {
		
		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		
		productName = new TextField("Product Name");
		productName.setWidthFull();
		productName.setRequired(true);
		productName.setRequiredIndicatorVisible(true);
		productName.setVisible(true);
		
		shortCode = new TextField("Product Short Code");
		shortCode.setWidthFull();
		shortCode.setRequired(true);
		shortCode.setRequiredIndicatorVisible(true);
		shortCode.setVisible(true);
		
		HorizontalLayout productInfoContainer = new HorizontalLayout();
		productInfoContainer.setAlignItems(Alignment.AUTO);
		productInfoContainer.setSizeFull();
		productInfoContainer.add(productName, shortCode);
		
		

		VerticalLayout newItemInvetoryContainer = new VerticalLayout();
		newItemInvetoryContainer.addClassNames("no-padding");
		
		
		
		product = new MultiSelectComboBox<>();
		product.setLabel("Products");
		product.setItems(products);
		product.setRequiredIndicatorVisible(true);
		product.setPlaceholder("Select Products");
		product.setItemLabelGenerator(e -> e.getProductName());
		product.setWidthFull();
		product.addValueChangeListener(value -> {
			if (!value.getHasValue().isEmpty()) {
				
				publishButton.setEnabled(true);
				Set<Product> newValue = value.getValue();
				Set<Product> oldValue = value.getOldValue();
				
				
				Set<Product> added = Sets.difference(newValue, oldValue);
				Set<Product> deleted = Sets.difference(oldValue, newValue);
				
				for (Product product : added) {
					
					addNewInventorySection(newItemInvetoryContainer, product);
				}
				
				for (Product removedProduct : deleted) {
					
					Set<ProductInventorySubView> viewToDelete = Sets.newHashSet();
					productInventorySubViewSet.forEach(subView -> {
						
						if (subView.getProduct().getProductName().equalsIgnoreCase(removedProduct.getProductName())) {
							newItemInvetoryContainer.remove(subView);
							viewToDelete.add(subView);
						}
					});
					
					productInventorySubViewSet.removeAll(viewToDelete);
				}
				
			}
		});
		

		
		HorizontalLayout productSpecsContainer = new HorizontalLayout();
		productSpecsContainer.setAlignItems(Alignment.AUTO);
		productSpecsContainer.setSizeFull();
		productSpecsContainer.add(product);


		HorizontalLayout actionsButtonLayout = new HorizontalLayout();
		actionsButtonLayout.setAlignItems(Alignment.END);
		actionsButtonLayout.setJustifyContentMode(JustifyContentMode.END);
		actionsButtonLayout.addClassNames("padding-top-large", "full-width");
		cancelButton = new Button("Cancel");
		
		cancelButton.addClickListener(e -> {
			UI.getCurrent().navigate(StocksInvetoryView.class);
			
		});
		publishButton = new Button("Save");
		publishButton.setEnabled(false);
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton.addClickListener(e -> {
			HashSet<ItemStock> stocksToUpdate = Sets.newHashSet();
			
			for (ProductInventorySubView view : productInventorySubViewSet) {
				
				HashSet<ItemStock> stock = view.extractFieldValues();
				stocksToUpdate.addAll(stock);
			}
			
			itemStockService.updateAll(stocksToUpdate);
			
			UI.getCurrent().navigate(StocksInvetoryView.class);
		});
		
		
		actionsButtonLayout.add(cancelButton, publishButton);
		VerticalLayout addSizeButtonLayout = new VerticalLayout();
		addSizeButtonLayout.addClassNames("no-padding", "padding-top-large");
		//addSizeButtonLayout.add(addSizeButton);
		addSizeButtonLayout.add(actionsButtonLayout);

		content.add(productSpecsContainer, newItemInvetoryContainer, addSizeButtonLayout);

	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	}
	

	private void addNewInventorySection(VerticalLayout addSizeButtonLayout, Product product) {
		ProductInventorySubView pricingSubView = new ProductInventorySubView(product, user);
		productInventorySubViewSet.add(pricingSubView);
		addSizeButtonLayout.add(pricingSubView);

	}
	

	

}
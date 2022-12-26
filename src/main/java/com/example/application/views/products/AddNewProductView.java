package com.example.application.views.products;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.Categories;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.entity.stock.ItemStock;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.data.service.products.SizesService;
import com.example.application.data.service.stock.ItemStockService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.products.components.SizePricingSubView;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Add New Product")
@Route(value = "addProduct", layout = MainLayout.class)
@PermitAll
public class AddNewProductView extends AbstractPfdiView implements HasComponents, HasStyle {

	private static final long serialVersionUID = -6210105239749320428L;

	private TextField productName;
	private TextField shortCode;
	private Select<Category> category;
	private MultiSelectComboBox<Size> sizes;
	private Button cancelButton;
	//private Button saveAsDraftButton;
	private Button publishButton;
	private CategoryService categoryService;
	private ProductService productService;
	private HashSet<SizePricingSubView> pricingSubViewSet = new HashSet<>();
	private SizesService sizesService;
	private ItemStockService itemStockService;
	private AuthenticatedUser authenticatedUser;

	@Autowired
	public AddNewProductView(AuthenticatedUser authenticatedUser, CategoryService categoryService, ProductService productService, SizesService sizesService, ItemStockService itemStockService) {
		super("add-new-product", "Add New Product");
		addClassNames("products-view");
		this.categoryService = categoryService;
		this.productService = productService;
		this.sizesService = sizesService;
		this.itemStockService = itemStockService;
		this.authenticatedUser = authenticatedUser;
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}

	private void createMainContent(VerticalLayout content) {
		HorizontalLayout categoryNameImageWrapper = new HorizontalLayout();
		categoryNameImageWrapper.setWidth("100%");
		
		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H2 header = new H2("Product Details");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		Div div = new Div();
		div.addClassNames("background-div");
		div.setHeight("141px");
		div.setWidth("247px");

		Image image = new Image();
		image.setWidth("100%");

		div.add(image);

		VerticalLayout newAddSizeContainer = new VerticalLayout();
		VerticalLayout nameAndCategoryWrapper = new VerticalLayout();
		nameAndCategoryWrapper.addClassName("add-product-name-wrapper");
		
		

		
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
		

		
		category = new Select<>();
		category.setLabel("Category");
		category.setEmptySelectionAllowed(false);

		category.setItems(categoryService.listAll(Sort.by("id")));
		category.setRequiredIndicatorVisible(true);
		category.setEmptySelectionAllowed(false);
		category.setPlaceholder("Select Category");
		category.setItemLabelGenerator(e -> e.getCategoryName());
		category.setWidthFull();
		category.addValueChangeListener(e -> {
			if (!e.getHasValue().isEmpty()) {
				if (Categories.Flavors.name().equals(e.getValue().getCategoryType())) {
					productName.setLabel("Flavor Name");
					shortCode.setLabel("Flavor Short Code");
				} else if (Categories.Cones.name().equals(e.getValue().getCategoryType())) {
					productName.setLabel("Cone Name");
					shortCode.setLabel("Cone Short Code");
				} else {
					productName.setLabel("Other Product Name");
					shortCode.setLabel("Other Product Short Code");
				}
				sizes.setItems(e.getValue().getSizeSet());
				productName.setVisible(true);
				shortCode.setVisible(true);
				pricingSubViewSet.clear();
				newAddSizeContainer.removeAll();
				
			}
		});
		
		sizes = new MultiSelectComboBox<>();
		sizes.setLabel("Sizes");

		//sizes.setItems(sizesService.listAll(Sort.by("id")));
		sizes.setRequiredIndicatorVisible(true);
		sizes.setPlaceholder("Select Sizes");
		sizes.setItemLabelGenerator(e -> e.getSizeName());
		sizes.setWidthFull();
		sizes.addValueChangeListener(e -> {
			
			Set<Size> newValue = e.getValue();
			Set<Size> oldValue = e.getOldValue();
			
			
			Set<Size> added = Sets.difference(newValue, oldValue);
			Set<Size> deleted = Sets.difference(oldValue, newValue);
			
			for (Size size : added) {
				addNewSizeFormSection(newAddSizeContainer, size);
			}
			
			for (Size removedSize : deleted) {
				pricingSubViewSet.forEach(subView -> {
					if (subView.getSize().getSizeName().equalsIgnoreCase(removedSize.getSizeName())) {
						newAddSizeContainer.remove(subView);
					}
				});
			}
		}); //TODO add a prompt that will ask the user if they want to change the selection
		
		HorizontalLayout productSpecsContainer = new HorizontalLayout();
		productSpecsContainer.setAlignItems(Alignment.AUTO);
		productSpecsContainer.setSizeFull();
		productSpecsContainer.add(category, sizes);

		nameAndCategoryWrapper.add(productSpecsContainer, productInfoContainer);

		VerticalLayout sizeContainer = new VerticalLayout();

		VerticalLayout iceCreamDescriptionContainer = new VerticalLayout();
		iceCreamDescriptionContainer.setAlignItems(Alignment.START);
		iceCreamDescriptionContainer.add(nameAndCategoryWrapper);

		categoryNameImageWrapper.add(div, nameAndCategoryWrapper);

		HorizontalLayout actionsButtonLayout = new HorizontalLayout();
		actionsButtonLayout.setAlignItems(Alignment.END);
		actionsButtonLayout.setJustifyContentMode(JustifyContentMode.END);
		actionsButtonLayout.addClassNames("padding-top-large", "full-width");
		cancelButton = new Button("Cancel");
		
		cancelButton.addClickListener(e -> {
//			newAddSizeContainer.removeAll();
//			category.setValue(null);
//			productName.setValue(null);
			UI.getCurrent().navigate(ProductsView.class);
			
		});
		publishButton = new Button("Publish");
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton.addClickListener(e -> {

			Product product = createNewProduct();
			product = productService.update(product);
			itemStockService.updateAll(createInitialItemStockData(product));
			Notification.show("Successfully added new product: " + product.getProductName());
			UI.getCurrent().navigate(ProductsView.class);
		});
		
		
		actionsButtonLayout.add(cancelButton, publishButton);

		newAddSizeContainer.addClassNames("no-padding");
		VerticalLayout addSizeButtonLayout = new VerticalLayout();
		addSizeButtonLayout.addClassNames("no-padding", "padding-top-large");
		//addSizeButtonLayout.add(addSizeButton);
		addSizeButtonLayout.add(actionsButtonLayout);

		content.add(categoryNameImageWrapper, sizeContainer, newAddSizeContainer, addSizeButtonLayout);

	}

	private List<ItemStock> createInitialItemStockData(Product product) {
		
		
		ArrayList<ItemStock> itemStocks = new ArrayList<ItemStock>();
		
		for (Size size : sizes.getValue()) {
			ItemStock itemStock = new ItemStock();
			itemStock.setProduct(product);
			itemStock.setSize(size);
			itemStock.setUpdatedBy("test");
			itemStock.setAvailableStock(0);
			itemStock.setReservedStock(0);
			itemStocks.add(itemStock);
		}
		
		// TODO Auto-generated method stub
		return itemStocks;
	}

	private Product createNewProduct() {
		Product product = new Product();
		
		product.setProductName(productName.getValue());
		product.setProductShortCode(shortCode.getValue());
		product.setProductDescription("product test"); //TODO add field description
		product.setCategory(category.getValue());
		
		
		HashSet<ProductPrice> prices = new HashSet<>();
		for (SizePricingSubView pricingView : pricingSubViewSet ) {
			
			prices.addAll(pricingView.extractFieldValues(product));
			
		}
		
		product.setProductPrices(prices);	
		return product;
		
	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {

	}

	private void addNewSizeFormSection(VerticalLayout addSizeButtonLayout, Size size) {
		SizePricingSubView pricingSubView = new SizePricingSubView(size, category.getValue());
		pricingSubViewSet.add(pricingSubView);
		addSizeButtonLayout.add(pricingSubView);

	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	}
}
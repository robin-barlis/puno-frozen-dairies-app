package com.example.application.views.products;

import java.util.HashSet;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Product;
import com.example.application.data.entity.products.ProductPrice;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.ProductService;
import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.example.application.views.products.components.SizePricingSubView;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
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
	private Select<Category> category;
	private Button cancelButton;
	//private Button saveAsDraftButton;
	private Button publishButton;
	private CategoryService categoryService;
	private ProductService productService;
	private HashSet<SizePricingSubView> pricingSubViewSet = new HashSet<>();

	@Autowired
	public AddNewProductView(CategoryService categoryService, ProductService productService) {
		super("add-new-product", "Product Management > Add New Product");
		addClassNames("products-view");
		this.categoryService = categoryService;
		this.productService = productService;
		VerticalLayout content = new VerticalLayout();
		createMainContent(content);
		add(content);

	}

	private void createMainContent(VerticalLayout content) {
		HorizontalLayout categoryNameImageWrapper = new HorizontalLayout();
		categoryNameImageWrapper.setWidth("100%");

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
				pricingSubViewSet.clear();
				newAddSizeContainer.removeAll();
				for (Size size : e.getValue().getSizeSet()) {
					addNewSizeFormSection(newAddSizeContainer, size);
				}
			}
		});

		nameAndCategoryWrapper.add(productName, category);

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
			newAddSizeContainer.removeAll();
			category.setValue(null);
			productName.setValue(null);
			UI.getCurrent().navigate(ProductsView.class);
			
		});
		//saveAsDraftButton = new Button("Save as draft");
		//saveAsDraftButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton = new Button("Publish");
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton.addClickListener(e -> {
			Product product = createNewProduct();
			productService.update(product);
			Notification.show("Successfully added new product: " + product.getProductName());
			UI.getCurrent().navigate(ProductsView.class);
		});
		
		
		actionsButtonLayout.add(cancelButton, publishButton);

		newAddSizeContainer.addClassNames("no-padding");
//		addSizeButton = new Button("Add Size Pricing");
//		addSizeButton.addClickListener(e -> {
//
//			if (sizeFormCreatedCount == addSizeLimit) {
//				addSizeButton.setEnabled(false);
//			}
//			addNewSizeFormSection(sizeFormCreatedCount, newAddSizeContainer);
//			sizeFormCreatedCount++;
//		});
//		addSizeButton.setEnabled(false);
		VerticalLayout addSizeButtonLayout = new VerticalLayout();
		addSizeButtonLayout.addClassNames("no-padding", "padding-top-large");
		//addSizeButtonLayout.add(addSizeButton);
		addSizeButtonLayout.add(actionsButtonLayout);

		content.add(categoryNameImageWrapper, sizeContainer, newAddSizeContainer, addSizeButtonLayout);

	}

	private Product createNewProduct() {
		Product product = new Product();
		
		product.setProductName(productName.getValue());
		product.setProductDescription("product test"); //TODO add field description
		
		
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
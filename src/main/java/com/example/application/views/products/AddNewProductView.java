package com.example.application.views.products;

import java.util.ArrayList;

import javax.annotation.security.PermitAll;

import com.example.application.views.AbstractPfdiView;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
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
	private Select<String> category;
	private Button addSizeButton;
	private Button cancelButton;
	private Button saveAsDraftButton;
	private Button publishButton;
	
	public AddNewProductView() {
		super("add-new-product", "Product Management > Add New Product");
		addClassNames("products-view");

	}

	@Override
	protected void createMainContentLayout(VerticalLayout mainContent) {

		HorizontalLayout categoryNameImageWrapper = new HorizontalLayout();
		categoryNameImageWrapper.setWidth("100%");

		Div div = new Div();
		div.addClassNames("background-div");
		div.setHeight("141px");
		div.setWidth("247px");
		
		Image image = new Image();
		image.setWidth("100%");

		div.add(image);
		
		VerticalLayout nameAndCategoryWrapper = new VerticalLayout();
		nameAndCategoryWrapper.addClassName("add-product-name-wrapper");
		productName = new TextField("Product Name");
		productName.setWidthFull();
		productName.setRequired(true);
		productName.setRequiredIndicatorVisible(true);
		
		category = new Select<>();
		category.setLabel("Category");
		category.setEmptySelectionAllowed(false);
		ArrayList<String> sampleItems = new ArrayList<>();
		sampleItems.add("test");
		sampleItems.add("test2");
		category.setItems(sampleItems);
		category.setRequiredIndicatorVisible(true);
		category.setEmptySelectionAllowed(false);
		category.setPlaceholder("Select Category");
		category.setWidthFull();
		
		nameAndCategoryWrapper.add(productName, category);
		
		VerticalLayout sizeContainer = new VerticalLayout();
		
		

		VerticalLayout iceCreamDescriptionContainer = new VerticalLayout();
		iceCreamDescriptionContainer.setAlignItems(Alignment.START);
		iceCreamDescriptionContainer.add(nameAndCategoryWrapper);

		categoryNameImageWrapper.add(div, nameAndCategoryWrapper);
		
		addSizeButton = new Button("Add Size");
		addSizeButton.addClickListener(e-> createSizeForm(sizeContainer));
		
		
		VerticalLayout addSizeButtonLayout = new VerticalLayout();
		addSizeButtonLayout.addClassNames("no-padding","padding-top-large");
		addSizeButtonLayout.add(addSizeButton);
		
		
		HorizontalLayout actionsButtonLayout = new HorizontalLayout();
		actionsButtonLayout.setAlignItems(Alignment.END);
		actionsButtonLayout.setJustifyContentMode(JustifyContentMode.END);
		actionsButtonLayout.addClassNames("padding-top-large", "full-width");
		cancelButton = new Button("Cancel");
		
		saveAsDraftButton = new Button("Save as draft");
		saveAsDraftButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		
		publishButton = new Button("Publish");
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		actionsButtonLayout.add(cancelButton, saveAsDraftButton, publishButton);
		
		addSizeButtonLayout.add(actionsButtonLayout);
		
		
	
	
		
		
		
		
		
		mainContent.add(categoryNameImageWrapper, sizeContainer, addSizeButtonLayout);

	}

	private Object createSizeForm(VerticalLayout sizeContainer) {
	
		return null;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

	}
}
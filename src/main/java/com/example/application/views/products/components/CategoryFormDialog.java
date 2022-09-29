package com.example.application.views.products.components;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.SizesService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.spring.annotation.UIScope;

@Component
@UIScope
public class CategoryFormDialog  extends Dialog {
	
	private static final long serialVersionUID = -4979016979015013531L;
	private Button saveButton;
	private Button cancelButton;
	private TextField categoryName;
	private TextField categoryDescription;
	private BeanValidationBinder<Category> binder;
	private Category category;
	
	public CategoryFormDialog(String label, CategoryService categoryService, SizesService sizeService) {
		Label categoryLabel = new Label(label);
		categoryLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		Hr divider2 = new Hr();

		categoryName = new TextField("Category Name");
		categoryName.setRequired(true);
		categoryName.setRequiredIndicatorVisible(true);
		
		categoryDescription = new TextField("Category Description");
		categoryDescription.setRequired(true);
		categoryDescription.setRequiredIndicatorVisible(true);
		
		MultiSelectComboBox<Size> categoryComboBox = new MultiSelectComboBox<>("Sizes");
		categoryComboBox.setItems(sizeService.listAll(Sort.unsorted()));
		categoryComboBox.setItemLabelGenerator(Size::getSizeName);
		categoryComboBox.setWidthFull();
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		binder = new BeanValidationBinder<>(Category.class);
		
		binder.bind(categoryName, "categoryName");
		binder.bind(categoryDescription, "categoryDescription");
		saveButton.addClickListener(e -> {
			try {
				category = new Category();
				binder.writeBean(category);
				category.setSizeSet(categoryComboBox.getValue());

				this.category = categoryService.update(category);

				Notification.show("Category successfully created")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				this.close();
			} catch (ValidationException validationException) {
				Notification.show("An exception happened while trying to store the samplePerson details.");
			}
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			this.close();
			clearForm();
		});

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(categoryLabel, divider1, categoryName,  categoryDescription, categoryComboBox,
				divider2, saveButton, cancelButton);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(categoryComboBox, 2);
		formLayout.setColspan(categoryLabel, 2);
		formLayout.setColspan(divider1, 2);
		formLayout.setColspan(divider2, 2);

		add(formLayout);
	}

	private void clearForm() {
		this.categoryName.clear();
		this.categoryDescription.clear();
		category = null;
		
	}
	
	
	public Category getUpdatedCategory() {
		return category;
	}
	
	

}

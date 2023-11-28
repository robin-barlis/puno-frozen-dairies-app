package com.example.application.views.products.components;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.application.data.Categories;
import com.example.application.data.entity.products.Category;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CategoryService;
import com.example.application.data.service.products.SizesService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.spring.annotation.UIScope;


@Component
@UIScope
public class CategoryFormDialog  extends ConfirmDialog {
	
	private static final long serialVersionUID = -4979016979015013531L;
	private Button saveButton;
	private Button cancelButton;
	private TextField categoryName;
	private Select<String> categoryType;
	private BeanValidationBinder<Category> binder;
	private Category category;
	private MultiSelectComboBox<Size> sizeComboBox ;
	
	public CategoryFormDialog(String label, CategoryService categoryService, SizesService sizeService) {
		Label categoryLabel = new Label(label);
		categoryLabel.getStyle().set("padding-bottom", "20px");
		
		List<Size> sizes = sizeService.listAll(Sort.unsorted());
		

		Hr divider1 = new Hr();


		categoryName = new TextField("Category Name");
		categoryName.setRequired(true);
		categoryName.setRequiredIndicatorVisible(true);
		categoryType = new Select<>();
		categoryType.setLabel("Category Type");
		categoryType.setEmptySelectionAllowed(false);

		List<String> enumNames = Stream.of(Categories.values())
                .map(Categories::name)
                .collect(Collectors.toList());
		categoryType.setItems(enumNames);
		categoryType.setRequiredIndicatorVisible(true);
		categoryType.setPlaceholder("Select Category Type");
		categoryType.setWidthFull();
		categoryType.setEnabled(true);
		categoryType.addValueChangeListener(e-> {
			sizeComboBox.setEnabled(true);
			
			List<Size> sizeByType = sizes.stream().filter(s -> {
				return s.getSizeCategory().equalsIgnoreCase(categoryType.getValue());
			}).toList();
			sizeComboBox.setItems(sizeByType);
		
		});
		
		sizeComboBox = new MultiSelectComboBox<>("Sizes");
		sizeComboBox.setPlaceholder("Set Sizes");
		sizeComboBox.setEnabled(sizeComboBox.getValue().size() > 0);
		sizeComboBox.setItemLabelGenerator(Size::getSizeName);
		sizeComboBox.setWidthFull();
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setEnabled(!StringUtils.isEmpty(categoryName.getValue()));
		
		
		categoryType.addValueChangeListener( e-> {
			saveButton.setEnabled(!StringUtils.isEmpty(categoryName.getValue()));
		});
		
		categoryName.addValueChangeListener(e-> {

			saveButton.setEnabled(!StringUtils.isEmpty(categoryName.getValue()));
		});
		binder = new BeanValidationBinder<>(Category.class);
		
		binder.bind(categoryName, "categoryName");
		binder.bind(categoryType, "categoryType");
		saveButton.addClickListener(e -> {
			try {
				if (category == null) {
					category = new Category();				
				}
				binder.writeBean(category);
				category.setSizeSet(sizeComboBox.getValue());

				this.category = categoryService.update(category);

				Notification.show("Category successfully created")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				clearForm(false);
				this.close();
			} catch (ValidationException validationException) {
				Notification.show("An exception happened while trying to store the samplePerson details.");
			}
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			this.close();
			clearForm(true);
		});

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(categoryLabel, divider1, categoryName,  categoryType, sizeComboBox);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(sizeComboBox, 2);
		formLayout.setColspan(categoryLabel, 2);
		formLayout.setColspan(divider1, 2);
		
		setCancelable(true);
		setConfirmButton(saveButton);
		setCancelButton(cancelButton);

		add(formLayout);
	}

	public void clearForm(boolean removeObject) {
		this.categoryName.clear();
		this.categoryType.clear();
		this.sizeComboBox.clear();
		
		if (removeObject) {
			category = null;
		}
		
	}
	
	
	public Category getUpdatedCategory() {
		return category;
	}
	


	public void setCurrentSelectionToBinder(Category currentCategory) {
		this.category = currentCategory;
		binder.readBean(currentCategory);
		
		sizeComboBox.setValue(currentCategory.getSizeSet());
	}
	
	

}

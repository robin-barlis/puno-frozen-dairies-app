package com.example.application.views.products.components;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.application.data.Categories;
import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CustomerTagService;
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
public class SizeFormDialog  extends ConfirmDialog {
	
	private static final long serialVersionUID = -4979016979015013531L;
	private Button saveButton;
	private Button cancelButton;
	private TextField sizeName;
	private TextField sizeDescription;
	private Select<String> sizeCategory;
	private BeanValidationBinder<Size> binder;
	private Size size;
	MultiSelectComboBox<CustomerTag> customerTagComboBox;
	
	public SizeFormDialog(String label, CustomerTagService customerTagService, SizesService sizeService) {
		Label addTagLabel = new Label(label);
		addTagLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		Hr divider2 = new Hr();

		sizeName = new TextField("Size Name");
		sizeName.setRequired(true);
		sizeName.setRequiredIndicatorVisible(true);
		
		sizeDescription = new TextField("Size Description");
		
		sizeCategory = new Select<String>();
		sizeCategory.setLabel("Size Type");
		sizeCategory.setItems(Stream.of(Categories.values())
                .map(Categories::name)
                .collect(Collectors.toList()));
		sizeCategory.setRequiredIndicatorVisible(true);
		
		customerTagComboBox = new MultiSelectComboBox<>("Add Customer Tags");
		customerTagComboBox.setItems(customerTagService.listAll(Sort.unsorted()));
		customerTagComboBox.setItemLabelGenerator(CustomerTag::getCustomerTagName);
		customerTagComboBox.setWidthFull();
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setEnabled(!StringUtils.isEmpty(sizeName.getValue()));
		binder = new BeanValidationBinder<>(Size.class);
		
		sizeName.addValueChangeListener(e-> {
			saveButton.setEnabled(!StringUtils.isEmpty(sizeName.getValue()));
		});
		
		binder.bind(sizeName, "sizeName");
		binder.bind(sizeDescription, "sizeDescription");
		binder.bind(sizeCategory, "sizeCategory");
		saveButton.addClickListener(e -> {
			try {
				if (size == null) {

					size = new Size();
					size.setSizeOrder(0);
				}
				binder.writeBean(size);
				size.setCustomerTagSet(customerTagComboBox.getValue());

				this.size = sizeService.update(size);
				customerTagComboBox.deselectAll();
				Notification.show("Customer successfully created")
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
		formLayout.add(addTagLabel, divider1, sizeName,  sizeDescription, sizeCategory, customerTagComboBox,
				divider2);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(customerTagComboBox, 2);
		formLayout.setColspan(addTagLabel, 2);
		formLayout.setColspan(divider1, 2);
		
		setConfirmButton(saveButton);
		setCancelButton(cancelButton);
		setCancelable(true);

		add(formLayout);
	}

	private void clearForm(boolean removeObject) {
		this.sizeName.clear();
		this.sizeDescription.clear();
		this.sizeCategory.clear();
		if (removeObject) {
			size = null;	
		}
		
	}
	
	
	public Size getUpdateSize() {
		return size;
	}

	public void setCurrentSelectionToBinder(Size currentSize) {
		
		this.size = currentSize;
		if (currentSize != null) {

			binder.readBean(currentSize);
			
			customerTagComboBox.setValue(currentSize.getCustomerTagSet());
		}
	}
	
	

}

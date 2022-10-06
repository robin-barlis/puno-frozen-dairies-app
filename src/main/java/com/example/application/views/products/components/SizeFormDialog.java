package com.example.application.views.products.components;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.CustomerTagService;
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
public class SizeFormDialog  extends Dialog {
	
	private static final long serialVersionUID = -4979016979015013531L;
	private Button saveButton;
	private Button cancelButton;
	private TextField sizeName;
	private TextField sizeDescription;
	private BeanValidationBinder<Size> binder;
	private Size size;
	private int sizeLimit;
	
	public SizeFormDialog(String label, CustomerTagService customerTagService, SizesService sizeService) {
		Label addTagLabel = new Label(label);
		addTagLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		Hr divider2 = new Hr();

		sizeName = new TextField("Size Name");
		sizeName.setRequired(true);
		sizeName.setRequiredIndicatorVisible(true);
		
		sizeDescription = new TextField("Size Description");
		sizeDescription.setRequired(true);
		sizeDescription.setRequiredIndicatorVisible(true);
		
		MultiSelectComboBox<CustomerTag> customerTagComboBox = new MultiSelectComboBox<>("Add Location Tags");
		customerTagComboBox.setItems(customerTagService.listAll(Sort.unsorted()));
		customerTagComboBox.setItemLabelGenerator(CustomerTag::getCustomerTagName);
		customerTagComboBox.setWidthFull();
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		binder = new BeanValidationBinder<>(Size.class);
		
		binder.bind(sizeName, "sizeName");
		binder.bind(sizeDescription, "sizeDescription");
		saveButton.addClickListener(e -> {
			try {
				size = new Size();
				binder.writeBean(size);
				size.setCustomerTagSet(customerTagComboBox.getValue());

				this.size = sizeService.update(size);

				Notification.show("Customer successfully created")
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
		formLayout.add(addTagLabel, divider1, sizeName,  sizeDescription, customerTagComboBox,
				divider2, saveButton, cancelButton);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(customerTagComboBox, 2);
		formLayout.setColspan(addTagLabel, 2);
		formLayout.setColspan(divider1, 2);
		formLayout.setColspan(divider2, 2);

		add(formLayout);
	}

	private void clearForm() {
		this.sizeName.clear();
		this.sizeDescription.clear();
		size = null;
		
	}
	
	
	public Size getUpdateSize() {
		return size;
	}
	
	

}

package com.example.application.views.products.components;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.LocationTag;
import com.example.application.data.service.products.CustomerTagService;
import com.example.application.data.service.products.LocationTagService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
public class CustomerTagFormDialog  extends ConfirmDialog {
	
	private static final long serialVersionUID = -4979016979015013531L;
	private Button saveButton;
	private Button cancelButton;
	private TextField customerTagName;
	private TextField customerTagDescription;
	private BeanValidationBinder<CustomerTag> binder;
	private CustomerTag customerTag;
	private MultiSelectComboBox<LocationTag> locationTagComboBox;
	private LocationTagService locationTagService;
	
	public CustomerTagFormDialog(String label, CustomerTagService customerTagService, LocationTagService locationTagService) {
		this.locationTagService = locationTagService;
		Label addTagLabel = new Label(label);
		addTagLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		customerTagName = new TextField("Customer Tag Name");
		customerTagName.setRequired(true);
		customerTagName.setRequiredIndicatorVisible(true);
		
		customerTagDescription = new TextField("Customer Tag Description");
		
		locationTagComboBox = new MultiSelectComboBox<>("Add Location Tags");
		locationTagComboBox.setItems(locationTagService.listAll(Sort.unsorted()));
		locationTagComboBox.setItemLabelGenerator(LocationTag::getLocationTagName);
		locationTagComboBox.setWidthFull();
		

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setEnabled(!StringUtils.isEmpty(customerTagName.getValue()));
		
		customerTagName.addValueChangeListener(e -> {
			saveButton.setEnabled(!StringUtils.isEmpty(customerTagName.getValue()));
		});
		
		
		binder = new BeanValidationBinder<>(CustomerTag.class);
		
		binder.bind(customerTagName, "customerTagName");
		binder.bind(customerTagDescription, "customerTagDescription");
		binder.bind(locationTagComboBox, "locationTagSet");
		saveButton.addClickListener(e -> {
			try {
				if (customerTag == null) {
					customerTag = new CustomerTag();
				}
				binder.writeBean(customerTag);
				customerTag.setLocationTagSet(locationTagComboBox.getValue());

				this.customerTag = customerTagService.update(customerTag);

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
		});

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addTagLabel, divider1, customerTagName,  customerTagDescription, locationTagComboBox);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(locationTagComboBox, 2);
		formLayout.setColspan(addTagLabel, 2);
		formLayout.setColspan(divider1, 2);

		add(formLayout);

		setConfirmButton(saveButton);
		setCancelButton(cancelButton);
		setCancelable(true);
	}

	public void clearForm(boolean removeCustomerTag) {
		this.customerTagName.clear();
		this.customerTagDescription.clear();
		if (removeCustomerTag) {
			customerTag = null;
		}
		
	}
	
	
	public CustomerTag getUpdatedTag() {
		return customerTag;
	}
	
	@Override
	public void open() {
		// TODO Auto-generated method stub
		super.open();
		locationTagComboBox.setItems(locationTagService.listAll(Sort.unsorted()));
	}

	public void setCurrentCustomerSelectionToBinder(CustomerTag currentCustomerTag) {
		this.customerTag = currentCustomerTag;
		binder.readBean(currentCustomerTag);
		
		
		locationTagComboBox.setValue(currentCustomerTag.getLocationTagSet());
	}
	
	

}

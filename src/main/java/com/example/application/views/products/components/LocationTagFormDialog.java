package com.example.application.views.products.components;

import org.springframework.stereotype.Component;

import com.example.application.data.entity.products.LocationTag;
import com.example.application.data.service.products.LocationTagService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
public class LocationTagFormDialog  extends ConfirmDialog {
	
	private static final long serialVersionUID = -4979016979015013531L;
	private Button saveButton;
	private Button cancelButton;
	private TextField locationTagName;
	private TextField locationTagDescription;
	private BeanValidationBinder<LocationTag> binder;
	private LocationTag locationTag;
	
	public LocationTagFormDialog(String label, LocationTagService locationTagService) {
		Label addTagLabel = new Label(label);
		addTagLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();

		locationTagName = new TextField("Location Tag Name");
		locationTagName.setRequired(true);
		locationTagName.setRequiredIndicatorVisible(true);
		
		locationTagDescription = new TextField("Location Tag Description");
		locationTagDescription.setRequired(true);
		locationTagDescription.setRequiredIndicatorVisible(true);

		saveButton = new Button("Save Tag");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		binder = new BeanValidationBinder<>(LocationTag.class);
		
		binder.bind(locationTagName, "locationTagName");
		binder.bind(locationTagDescription, "locationTagDescription");

		

		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("800px");
		formLayout.add(addTagLabel, divider1, locationTagName,  locationTagDescription);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		formLayout.setColspan(addTagLabel, 2);
		formLayout.setColspan(divider1, 2);
		add(formLayout);

		
		saveButton.addClickListener(e -> {
			try {
				if (locationTag == null) {
					locationTag = new LocationTag();
				}
				binder.writeBean(locationTag);

				this.locationTag = locationTagService.update(locationTag);

				Notification.show("Location Tag successfully created/updated")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				clearForm(false);
				this.close();
			} catch (ValidationException binder) {
				Notification.show("An exception happened while trying to store the details.");
			}
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			this.close();
		});
		setConfirmButton(saveButton);
		setCancelButton(cancelButton);
		setCancelable(true);
	}

	public void clearForm(boolean removeObject) {
		this.locationTagName.clear();
		this.locationTagDescription.clear();
		if (removeObject) {
			locationTag = null;	
		}
		
	}
	
	
	public LocationTag getUpdatedLocationTag() {
		return locationTag;
	}

	public void setCurrentLocationTagSelectionToBinder(LocationTag currentLocationTagTag) throws ValidationException {
		this.locationTag = currentLocationTagTag;
		binder.readBean(locationTag);
	}
	

}

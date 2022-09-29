package com.example.application.listeners;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;

public abstract class UserEventListener implements ComponentEventListener<ComponentEvent<Button>>{
	
	
	
	private static final long serialVersionUID = -3227077135061587554L;
	
	private boolean editMode;
	
	public boolean isEditMode() {
		return editMode;
	}
	
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

}

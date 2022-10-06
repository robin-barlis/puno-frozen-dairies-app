package com.example.application.views.products.components;

import com.example.application.data.entity.products.CustomerTag;
import com.example.application.data.entity.products.Size;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

public class CustomerTagFormSubView extends VerticalLayout {

	private static final long serialVersionUID = 4972944868110854428L;
	
	private Select<CustomerTag> customerTag;
	private Size size;

	public CustomerTagFormSubView(Size size) {
		this.size = size;
		
		createMainContent();
	}

	private void createMainContent() {
		VerticalLayout formWrapper = new VerticalLayout();
		
	}
}

package com.example.application.views;

import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
@Configurable
public abstract class AbstractPfdiView extends Main implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;
	
	private VerticalLayout contentHeaderContainer;
	private VerticalLayout contentContainer;
	private String headerName;

	public AbstractPfdiView(String className, String headerName) {
		this.headerName = headerName;
		addClassNames(className, "mx-auto", "pb-l", "px-l");

		this.contentHeaderContainer = createContentHeaderContainer();
		addChildrenToContentHeaderContainer(contentHeaderContainer);
		
		this.contentContainer = new VerticalLayout();
		//this.contentContainer.addClassNames("flex-layout");
		createMainContentLayout(this.contentContainer); 

		add(this.contentHeaderContainer);
		Hr hr = new Hr();
		hr.addClassName("hr-class-wrapper");
		add(hr);
		add(this.contentContainer);

	}

	protected VerticalLayout createContentHeaderContainer() {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addClassNames("flex-layout");	
		return verticalLayout;
	}
	
	protected void createContentContainer() {


	}
	
	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();
		headerContainer.addClassName("pfdi-header-container");
		
		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1(headerName);
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");
		
		headerContainer.add(headerNameWrapper);
		contentHeaderContainer.add(headerContainer);
	}

	protected void createMainContentLayout(VerticalLayout tableContent) {
		
	}

}

package com.example.application.views.products.components;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.vaadin.jchristophe.SortableConfig;
import org.vaadin.jchristophe.SortableGroupStore;
import org.vaadin.jchristophe.SortableLayout;
import com.example.application.data.entity.products.Size;
import com.example.application.data.service.products.SizesService;
import com.example.application.views.products.ProductsView;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class SizeSortingDialog extends Dialog {
	
	private static final long serialVersionUID = 1L;
	
	private List<Size> sizes;
	private SizesService sizeService;
	
    private SortableLayout todoSortableLayout;
	
	
	public SizeSortingDialog(SizesService sizeService) {	
		this.sizeService = sizeService;
		this.sizes = sizeService.listAll(Sort.by(Direction.ASC,"sizeOrder"));
		createDialog();
	}

	private void createDialog() {
		this.setWidth("800px");
		this.setHeight("75vh");
		
		Label addProfileLabel = new Label("Drag cards to sort sizes");
		
		addProfileLabel.getStyle().set("padding-bottom", "20px");

		Hr divider1 = new Hr();
		
		addClassName("trello");

        SortableConfig sortableConfig = new SortableConfig();
        sortableConfig.setGroupName("trello");
        sortableConfig.allowDragIn(true);
        sortableConfig.allowDragOut(true);
        sortableConfig.setAnimation(150);
        sortableConfig.setChosenClass("trello-sortable-chosen");
        sortableConfig.setDragClass("trello-sortable-drag");
        sortableConfig.setGhostClass("trello-sortable-ghost");
        SortableGroupStore group = new SortableGroupStore();
        TrelloColumn todoColumn = new TrelloColumn("");
        addTasks(todoColumn);

        todoSortableLayout = new SortableLayout(todoColumn, sortableConfig, group);
      
        Button saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
			
			List<Component> sortedFlavors = todoSortableLayout.getChildren().collect(Collectors.toList());	
			int index = 1;
			
			List<Size> updatedSizes = Lists.newArrayList();
			
			if (sortedFlavors != null && sortedFlavors.size() == 1) {
				List<Component> tasks = sortedFlavors.get(0).getChildren().collect(Collectors.toList());
				
				for (Component sort : tasks) {
					if (sort instanceof SizeTrelloTask) {
						SizeTrelloTask task = (SizeTrelloTask) sort;
						Size size = task.getSize();
						size.setSizeOrder(index);
						updatedSizes.add(size);
						index++;
						
					}
				}
			}
			
			sizeService.updateAll(updatedSizes);

			Notification.show("Size Sorting successfully updated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			this.close();	
			UI.getCurrent().navigate(ProductsView.class);
		
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(e -> {
			this.close();
		});
		
		Hr divider2 = new Hr();
		
		FormLayout buttonContainer = new FormLayout();
		buttonContainer.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
		buttonContainer.setWidthFull();
		buttonContainer.add(cancelButton, saveButton);

        add(addProfileLabel,divider1, todoSortableLayout, divider2, buttonContainer);
    }

    private void addTasks(TrelloColumn todoColumn) {
    	
    	 for (Size size : sizes) {
            todoColumn.add(new SizeTrelloTask(size));
        }
    }

	@Override
	public void open() {
		
		super.open();
	}
	
	private class TrelloColumn extends Div {

	    private static final long serialVersionUID = 1L;


	    public TrelloColumn(String title) {
	        Div titleBox = new Div();
	        titleBox.setText(title);
	        add(titleBox);
	    }
	}
	
	private class SizeTrelloTask extends Div {

		    private static final long serialVersionUID = 1L;
			private Size size;

		    public SizeTrelloTask(Size size) {
		    	this.size = size;
		        Div titleBlock = new Div();
		        titleBlock.setText(size.getSizeName() + " - " + size.getSizeDescription());
		        titleBlock.addClassName("trello__column__task__title");
		        Div descriptionBlock = new Div();
		        descriptionBlock.setText(size.getSizeCategory());
		        descriptionBlock.addClassName("trello__column__task__description");
		        

		        add(titleBlock, descriptionBlock);
		        addClassName("trello__column__task");
		    }

		    public Size getSize() {
		        return size;
		    }
		}

}

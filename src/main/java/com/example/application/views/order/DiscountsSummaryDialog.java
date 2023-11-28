package com.example.application.views.order;

import java.util.List;

import com.example.application.views.order.offerings.AbstractOffering;
import com.example.application.views.order.offerings.FixedAmountPercentageDiscount;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class DiscountsSummaryDialog extends Dialog {

	private static final long serialVersionUID = 1L;

	private Button saveButton;
	private Button cancelButton;
	private List<AbstractOffering> offerings = null;
	private Grid<AbstractOffering> grid = new Grid<>(AbstractOffering.class, false);

	@Override
	public void open() {
		super.open();
	}

	public DiscountsSummaryDialog(List<AbstractOffering> offerings) {
		this.offerings = offerings;
		this.setHeaderTitle("Manage Order Discounts");

		createDialogContent();

		getFooter().add(cancelButton);
		getFooter().add(saveButton);
		saveButton.addClickListener(e -> {
			this.close();
		});

		cancelButton.addClickListener(e -> {
			this.close();
		});
		this.setWidth("40%");
		this.setMaxHeight("70%");
	}

	public List<AbstractOffering> getOfferings() {
		return offerings;
	}

	public void setOfferings(List<AbstractOffering> offerings) {
		this.offerings = offerings;
	}

	public List<AbstractOffering> getUpdatedOfferings() {
		return offerings;
	}

	private void createDialogContent() {
		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		cancelButton = new Button("Cancel");
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		VerticalLayout discountGridContainer = new VerticalLayout();

		grid.addColumn(e -> {
			return e.getDiscountType().getDiscountLabel();
		}).setHeader("Discount Type");

		grid.addColumn(new ComponentRenderer<>(VerticalLayout::new, (layout, offering) -> {
			addValueToLayout(layout, offering);
		})).setHeader("Discount Given");

		grid.addColumn(new ComponentRenderer<>(Button::new, (button, offering) -> {
			button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
			button.addClickListener(e -> this.removeOffering(offering));
			button.setIcon(new Icon(VaadinIcon.TRASH));
		}));
		grid.setItems(offerings);

		discountGridContainer.add(grid);

		add(new Hr());
		add(discountGridContainer);

	}

	private void addValueToLayout(VerticalLayout layout, AbstractOffering discount) {
		if (discount instanceof FixedAmountPercentageDiscount) {
			FixedAmountPercentageDiscount fixedAmountPercentageDiscount = (FixedAmountPercentageDiscount) discount;
			Span span = new Span(fixedAmountPercentageDiscount.getValue().toString());

			layout.add(span);
		} else {
			layout.add(VaadinIcon.MALE.create());
		}
	}

	private void refreshGrid() {

		grid.getDataProvider().refreshAll();
	}

	private void removeOffering(AbstractOffering offering) {

		if (offering != null) {
			offerings.remove(offering);
			this.refreshGrid();
		}
	}

}

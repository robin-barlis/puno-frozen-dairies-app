package com.example.application.views.products.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ProductsViewCard extends ListItem {

    private static final long serialVersionUID = 2711778627059486451L;


	public ProductsViewCard(String text, String url) {
        addClassNames("bg-contrast-5", "flex", "flex-col", "items-start", "rounded-l");

        Div div = new Div();
        div.addClassNames("background-div", "flex items-center", "justify-center", "mb-m", "overflow-product-view-card",
                "rounded-top w-full");
        div.setHeight("242px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc(url);
        image.setAlt(text);
        image.addClassName("product-view-image-attributes");

        div.add(image);
        

        Span iceCreamName = new Span();
        iceCreamName.addClassNames("text-l", "font-semibold");
        iceCreamName.setText(text);

        Paragraph sizes = new Paragraph("Pint | Liter | Half Gallon");
        sizes.addClassName("my-m");

        Span category = new Span();
        category.getElement().setAttribute("theme", "badge");
        category.setText("Ice Cream - Special Flavor");
        
        VerticalLayout iceCreamDescriptionContainer = new VerticalLayout();
        iceCreamDescriptionContainer.setAlignItems(Alignment.START);
        iceCreamDescriptionContainer.add(iceCreamName, sizes, category );

        add(div, iceCreamDescriptionContainer);

    }
}

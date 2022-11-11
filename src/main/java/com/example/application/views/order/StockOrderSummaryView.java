package com.example.application.views.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableHead;
import org.vaadin.stefan.table.TableHeaderCell;
import org.vaadin.stefan.table.TableRow;

import com.example.application.data.OrderStatus;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.orders.Order;
import com.example.application.data.entity.orders.OrderItems;
import com.example.application.data.service.orders.OrdersService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.utils.PfdiUtil;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Stock Orders")
@Route(value = "order/stockOrderSummary/:id", layout = MainLayout.class)
@RouteAlias(value = "/order/stockOrderSummary/:id", layout = MainLayout.class)
@RolesAllowed({ "Admin", "Superuser", "ADMIN" })
@Uses(Icon.class)
public class StockOrderSummaryView extends VerticalLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2754507440441771890L;

	private Button saveAsDraft;
	
	private Button submit;
	
	private Button back;

	private OrdersService ordersService;
	
	private Order order;
	
	private String orderId;
	
	private Span stockOrderNumberSpam;
	private Span orderDate;
	private Span storeName;
	private Span address;	
	private Span ownerName;

	private Table table;

	private BigDecimal totalAmount = BigDecimal.valueOf(0);

	private Span totalAmountLabel;
	private AuthenticatedUser user;
	private MenuItem editMenu;
	private MenuItem checkMenu;
	private MenuItem reject;


	@Autowired
	public StockOrderSummaryView(OrdersService ordersService, AuthenticatedUser user) {
		this.ordersService = ordersService;
		this.user = user;
		addClassNames("administration-view");
		
		addChildrenToContentHeaderContainer(this);
		
		Div actionButtonDiv = new Div();
		actionButtonDiv.addClassName("action-button-wrapper");
		MenuBar menuBar = new MenuBar();
		menuBar.addClassName("float-right");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        editMenu = createIconItem(menuBar, VaadinIcon.PENCIL, "Edit");
        editMenu.addClickListener(e -> {
        	System.out.println("editing");
        });
        checkMenu = createIconItem(menuBar, VaadinIcon.CHECK_CIRCLE, "Check");
        checkMenu.addClickListener(e -> {
        	ConfirmDialog confirmDialog = new ConfirmDialog();
        	confirmDialog.setCancelable(true);
        	confirmDialog.setHeader("Are you sure you want to approve this Stock Order?");
        	confirmDialog.addConfirmListener(event -> {
        		
        		AppUser updatedBy = user.get().get();
        		order.setStatus(OrderStatus.CHECKED.getOrderStatusName());
        		order.setCheckedByUser(updatedBy);
        		order.setCheckedDate(LocalDateTime.now());
        		order.setUpdatedByUser(updatedBy);
        		order.setUpdatedDate(LocalDateTime.now());
        		ordersService.update(order);
        		
        		Notification.show("Successfully checked Order ID", 10000, Position.MIDDLE);
        	});
        	confirmDialog.open();
        });
        
        reject = createIconItem(menuBar, VaadinIcon.CLOSE_CIRCLE, "Send Back to Sales");
        reject.addClickListener(e -> {
        	ConfirmDialog confirmDialog = new ConfirmDialog();
      	
        	confirmDialog.setCancelable(true);
        	confirmDialog.setHeader("Are you sure you want to reject this Stock Order?");
        	
        	Button button = new Button("Confirm");
        	button.setEnabled(false);
        	button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);       	
        	
        	TextArea textArea = new TextArea();
        	textArea.setWidthFull();
        	textArea.setLabel("Please add a note for Sales personnel.");
        	textArea.setValueChangeMode(ValueChangeMode.EAGER);
        	textArea.addValueChangeListener(valueChangeListener-> {
        		button.setEnabled(true);
        		
        	});
        	
         	button.addClickListener(buttonClickListener -> {
        		if (textArea.getValue() != null) {
        			AppUser updatedBy = user.get().get();
            		order.setStatus(OrderStatus.FOR_EDITING.getOrderStatusName());
            		order.setCheckedByUser(updatedBy);
            		order.setCheckedDate(LocalDateTime.now());
            		order.setUpdatedByUser(updatedBy);
            		order.setUpdatedDate(LocalDateTime.now());
            		order.setNotes(textArea.getValue());
            		ordersService.update(order);
            		
            		Notification.show("Successfully checked Order ID", 10000, Position.MIDDLE);
            		confirmDialog.close();
        		} 
        	});
         	
         	confirmDialog.setConfirmButton(button);
        	confirmDialog.add(textArea);
        	confirmDialog.open();
        });
        
        actionButtonDiv.add(menuBar);
		add(actionButtonDiv);
        
		Div mainDiv = new Div();
		mainDiv.addClassName("order-summary-div");

		createSummaryHeader(mainDiv);
		
		createOrderDetailsDiv(mainDiv);

		add(mainDiv);
		
		Div buttonContainer = new Div();
		buttonContainer.addClassName("order-summary-button-container");
		
		back = new Button("Back");
		back.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		back.addClassNames("submit-order-button", "float-right");
		back.addClickListener(e -> {
			UI.getCurrent().navigate(StockOrderView.class);
		});
		
		saveAsDraft = new Button("Save As Draft");
		saveAsDraft.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		saveAsDraft.addClassNames("submit-order-button", "float-right");
		saveAsDraft.addClickListener(e -> {
			UI.getCurrent().navigate(StockOrderView.class);
		});
		submit = new Button("Submit");
		submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		submit.addClassNames("submit-order-button", "float-right");
		submit.addClickListener(e -> {
			order.setStatus("For Checking");
			order = ordersService.update(order);
			Notification.show("Stock Order #" + order.getStockOrderNumber() + " successfully created.");
			UI.getCurrent().navigate(StockOrderView.class);
		});
		
		buttonContainer.add(submit,saveAsDraft, back );
		
		add(buttonContainer);
	}

	private MenuItem createIconItem(MenuBar menu, VaadinIcon iconName, String tooltipText) {
		Icon icon = new Icon(iconName);
        MenuItem item = menu.addItem(icon);
        return item;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		orderId = event.getRouteParameters().get("id").get();
		

		order = ordersService.get(Integer.parseInt(orderId)).get();
		Integer stockOrderNumber = order.getStockOrderNumber();
		if (stockOrderNumber == null) {
			stockOrderNumber = generateStockOrderNumber();
			order.setStockOrderNumber(stockOrderNumber);
		}

		stockOrderNumberSpam.setText("Stock Order #" + stockOrderNumber);
		orderDate.setText(order.getCreationDate().toString());
		storeName.setText(order.getCustomer().getStoreName());
		address.setText(order.getCustomer().getAddress());
		ownerName.setText(order.getCustomer().getOwnerName());
		
		
		//set table summary content
		
		Map<String, List<OrderItems>> orderItems = order.getOrderItems().stream().collect(Collectors.groupingBy(orderItem -> orderItem.getItemInventory().getProduct().getProductName()));;
		
		
		orderItems.forEach((itemName, sizeDetails) -> {
			TableRow detailsRowFlavor = table.getBody().addRow();
			TableDataCell dataCell = detailsRowFlavor.addDataCell();
			dataCell.setText(itemName);
			dataCell.setColSpan(4);
			
			sizeDetails.forEach(item -> {
				String sizeName = item.getItemInventory().getSize().getSizeName();
				Integer quantity = item.getQuantity();
				
				BigDecimal unitPrice = item.getProductPrice();
				BigDecimal amount = item.getProductPrice().multiply(BigDecimal.valueOf(quantity));
				totalAmount = totalAmount.add(amount);
				
				TableRow itemDetailsRow = table.getBody().addRow();
				TableDataCell sizeDataCell = itemDetailsRow.addDataCell();
				sizeDataCell.setText(sizeName);
				sizeDataCell.addClassNames("text-align-left", "padding-left-50px");
				
				
				
				TableDataCell quantityDataCell = itemDetailsRow.addDataCell();
				quantityDataCell.setText(quantity.toString());
				quantityDataCell.addClassName("text-align-right");
				
				TableDataCell unitPriceDataCell = itemDetailsRow.addDataCell();
				unitPriceDataCell.setText(PfdiUtil.getFormatter().format(unitPrice));
				unitPriceDataCell.addClassName("text-align-right");
				
				TableDataCell totalAmountDataCell = itemDetailsRow.addDataCell();
				totalAmountDataCell.setText(PfdiUtil.getFormatter().format(amount));
				totalAmountDataCell.addClassName("text-align-right");
			});
		});
		
		totalAmountLabel.setText("Total Amount : " + PfdiUtil.getFormatter().format(totalAmount));
		
		if ("For Checking".equals(order.getStatus())) {
			submit.setVisible(false);
			saveAsDraft.setVisible(false);
		}
		
		checkMenu.setEnabled(order.getStatus().equals(OrderStatus.FOR_CHECKING.getOrderStatusName()) || order.getStatus().equals(OrderStatus.FOR_EDITING.getOrderStatusName()));
		editMenu.setEnabled(order.getStatus().equals(OrderStatus.FOR_CHECKING.getOrderStatusName()) || order.getStatus().equals(OrderStatus.FOR_EDITING.getOrderStatusName()) );
		reject.setEnabled(order.getStatus().equals(OrderStatus.FOR_CHECKING.getOrderStatusName()) || order.getStatus().equals(OrderStatus.FOR_EDITING.getOrderStatusName()));
	}

	private void createSummaryHeader(Div mainDiv) {
		
		System.out.println("here");
		
		Div header = new Div();	
		
		Div stockOrderDiv = new Div();
		stockOrderNumberSpam = new Span();
		
		orderDate = new Span();
		stockOrderDiv.add(stockOrderNumberSpam);
		stockOrderDiv.add(orderDate);
		orderDate.addClassName("float-right");
		
		
		stockOrderDiv.addClassName("stock-order-number-date-container");
		
		Div ownerDetailsWrapper = new Div();
		ownerDetailsWrapper.addClassName("owner-details-wrapper");
		
		Div storeNameDiv = new Div();
		Span storeNameLabel = new Span("Store Name : ");
		storeName = new Span();
		storeName.addClassName("bold-label");
		
		storeNameDiv.add(storeNameLabel);
		storeNameDiv.add(storeName);
		
		Div addressDiv = new Div();
		Span addressLabel = new Span("Store Address : ");
		address = new Span();
		address.addClassName("bold-label");
		addressDiv.add(addressLabel);
		addressDiv.add(address);
		
		Div ownerNameDiv = new Div();
		Span ownerNameLabel = new Span("Owner Name : ");
		ownerName = new Span();
		ownerName.addClassName("bold-label");
		ownerNameDiv.add(ownerNameLabel);
		ownerNameDiv.add(ownerName);
		
		
		header.add(stockOrderDiv);
		
		ownerDetailsWrapper.add(storeNameDiv);
		ownerDetailsWrapper.add(addressDiv);
		ownerDetailsWrapper.add(ownerNameDiv);
		
		mainDiv.add(header);
		mainDiv.add(ownerDetailsWrapper);
		mainDiv.add(new Hr());		
	}

	private void createOrderDetailsDiv(Div mainDiv) {
		
		table = new Table();
		table.addClassName("order-summary-table");

		TableHead head = table.getHead();
		
		TableRow headerRow = head.addRow();
		
		
		TableHeaderCell itemHeaderCell =  headerRow.addHeaderCell();
		itemHeaderCell.setText("Item");
		itemHeaderCell.addClassName("text-align-left");
		
		TableHeaderCell quantityHeaderCell = headerRow.addHeaderCell();
		quantityHeaderCell.setText("Quantity");
		quantityHeaderCell.addClassName("text-align-right");
		
		TableHeaderCell unitPriceHeaderCell = headerRow.addHeaderCell();
		unitPriceHeaderCell.setText("Unit Price");
		unitPriceHeaderCell.addClassName("text-align-right");
		
		TableHeaderCell totalAmountHeaderCell = headerRow.addHeaderCell();
		totalAmountHeaderCell.setText("Amount");
		totalAmountHeaderCell.addClassName("text-align-right");
		mainDiv.add(table);
		mainDiv.add(new Hr());
		
		Div totalAmountWrapper = new Div();
		totalAmountLabel = new Span();
		totalAmountWrapper.add(totalAmountLabel);
		totalAmountWrapper.addClassName("order-summary-total");
		
		mainDiv.add(totalAmountWrapper);
		storeName.addClassName("bold-label");
	}

	private Integer generateStockOrderNumber() {
		return ordersService.getLastId() + 1;
	}


	protected void addChildrenToContentHeaderContainer(VerticalLayout contentHeaderContainer) {
		Div headerWrapper = new Div();
		headerWrapper.addClassName("order-summary-header-wrapper");
		
		HorizontalLayout headerContainer = new HorizontalLayout();
		headerContainer.setWidthFull();

		FlexLayout headerNameWrapper = new FlexLayout();
		headerNameWrapper.setFlexDirection(FlexDirection.ROW);
		headerNameWrapper.setJustifyContentMode(JustifyContentMode.START);
		headerNameWrapper.setAlignItems(Alignment.CENTER);
		H1 header = new H1("Create New Order > Summary");
		header.addClassNames("mb-0", "mt-s", "text-xl");
		headerNameWrapper.add(header);
		headerNameWrapper.setWidth("50%");

		
		headerContainer.add(headerNameWrapper);
		headerWrapper.add(headerContainer);

		Hr hr = new Hr();

		
		Div hrWrapper = new Div();
		hrWrapper.addClassName("hr-div-class-wrapper");
		hrWrapper.add(hr);
		
		contentHeaderContainer.add(headerWrapper);
		contentHeaderContainer.add(hrWrapper);

	}

}

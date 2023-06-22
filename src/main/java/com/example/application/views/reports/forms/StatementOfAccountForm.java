package com.example.application.views.reports.forms;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.beust.jcommander.internal.Lists;
import com.example.application.data.StatementPeriod;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.customers.Customer;
import com.example.application.data.service.UserService;
import com.example.application.data.service.customers.CustomerService;
import com.example.application.utils.PfdiUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class StatementOfAccountForm extends FormLayout{
	
	private static final long serialVersionUID = 1L;
	private ComboBox<Integer> yearPicker;
	private ComboBox<Month> monthPicker;

	private ComboBox<Integer> yearToPicker;
	private ComboBox<Month> monthToPicker;
	

	private Map<ComboBox<Integer>, SelectedYearWrapper> selectedDateMap = Maps.newHashMap();
	private Button addYearButton;
	private RadioButtonGroup<StatementPeriod> radioGroup;
	private List<Customer> availableCustomers = Lists.newArrayList();
	private TextField accountName = new TextField();
	private Select<Customer> accountNameComboBox;
	private ComboBox<AppUser> checkedBy;
	private ComboBox<AppUser> approvedBy;
	
	@Autowired
	public StatementOfAccountForm(CustomerService customerService, UserService userService) {

		radioGroup = new RadioButtonGroup<>();
		radioGroup.addThemeVariants(RadioGroupVariant.MATERIAL_VERTICAL);

		radioGroup.setLabel("Specify Statement Account Period:");
		radioGroup.setItems(StatementPeriod.values());
		radioGroup.setValue(StatementPeriod.CURRENT_PERIOD);
		radioGroup.setWidthFull();

		LocalDate currentDate = LocalDate.now();

		radioGroup.setRenderer(new ComponentRenderer<>(periodType -> {
			String currentYearMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, getLocale())
					+ " " + currentDate.getYear();

			List<Integer> selectableYears = IntStream.range(2022, currentDate.getYear()).boxed()
					.collect(Collectors.toList());

			if (periodType == StatementPeriod.CURRENT_PERIOD) {
				VerticalLayout layout = new VerticalLayout();
				layout.add(new Span("Current Period - " + currentYearMonth));

				return layout;
			} else if (periodType == StatementPeriod.DATE_RANGE) {
				VerticalLayout layout = new VerticalLayout();
				yearPicker = new ComboBox<>();
				yearPicker.setItems(selectableYears);
				yearPicker.setPlaceholder("Select From Year");
				yearPicker.setEnabled(false);
				yearPicker.setValue(currentDate.getYear());

				monthPicker = new ComboBox<>();
				monthPicker.setItems(Month.values());
				monthPicker.setPlaceholder("Select From Month");
				monthPicker.setItemLabelGenerator(m -> m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
				monthPicker.setEnabled(false);
				monthPicker.setValue(currentDate.getMonth());

				yearToPicker = new ComboBox<>();
				yearToPicker.setItems(selectableYears);
				yearToPicker.setPlaceholder("Select From Year");
				yearToPicker.setEnabled(false);
				yearToPicker.setValue(currentDate.getYear());

				monthToPicker = new ComboBox<>();
				monthToPicker.setItems(Month.values());
				monthToPicker.setPlaceholder("Select From Month");
				monthToPicker.setItemLabelGenerator(m -> m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
				monthToPicker.setEnabled(false);
				monthToPicker.setValue(currentDate.getMonth());

				HorizontalLayout fromContainer = new HorizontalLayout();
				fromContainer.setVerticalComponentAlignment(Alignment.START, yearPicker, monthPicker);
				Span fromSpan = new Span("From ");
				fromSpan.setWidth("50px");
				fromContainer.add(fromSpan, yearPicker, monthPicker);

				HorizontalLayout toContainer = new HorizontalLayout();
				toContainer.setVerticalComponentAlignment(Alignment.START, yearToPicker, monthToPicker);
				Span toSpan = new Span("To ");
				toSpan.setWidth("50px");
				toContainer.add(toSpan, yearToPicker, monthToPicker);

				layout.add(fromContainer, toContainer);
				return layout;
			} else if (periodType == StatementPeriod.SELECTED_DATE) {
				VerticalLayout layout = new VerticalLayout();
				VerticalLayout toContainer = new VerticalLayout();
	
				SelectedYearWrapper newSelectedDate = new SelectedYearWrapper(selectableYears);
				toContainer.add(newSelectedDate);
				selectedDateMap.put(newSelectedDate.getYear(), newSelectedDate);
				Button removeButton = newSelectedDate.getRemoveButton();
				removeButton.addClickListener(event -> {
					toContainer.remove(newSelectedDate);

					selectedDateMap.remove(newSelectedDate.getYear());
				});
				
				
				
				addYearButton = new Button("Add Year");
				addYearButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
				addYearButton.setEnabled(false);
				addYearButton.addClickListener(event -> {
					

					SelectedYearWrapper selectePeriodWrapper = new SelectedYearWrapper(selectableYears);
					toContainer.add(selectePeriodWrapper);
					selectedDateMap.put(selectePeriodWrapper.getYear(), selectePeriodWrapper);
					selectePeriodWrapper.enableChildren();
					
					
					Button button = selectePeriodWrapper.getRemoveButton();
					button.addClickListener(ev -> {
						toContainer.remove(selectePeriodWrapper);

						selectedDateMap.remove(selectePeriodWrapper.getYear());
					});
					

				});

				HorizontalLayout buttonContainer = new HorizontalLayout();
				buttonContainer.add(addYearButton);

				Span selectedDateTitle = new Span("Select Statement Periods");
				layout.add(selectedDateTitle, toContainer, buttonContainer);
				return layout;
			}
			return new Icon(VaadinIcon.FEMALE);
		}));

		radioGroup.addValueChangeListener(radioGroupValue -> {
			
			if (StatementPeriod.DATE_RANGE.equals(radioGroup.getValue())) {

				yearPicker.setEnabled(true);
				monthPicker.setEnabled(true);
				yearToPicker.setEnabled(true);
				monthToPicker.setEnabled(true);

				selectedDateMap.values().forEach(SelectedYearWrapper::disableChildren);

				addYearButton.setEnabled(false);
			} else if (StatementPeriod.SELECTED_DATE.equals(radioGroup.getValue())) {

				yearPicker.setEnabled(false);
				monthPicker.setEnabled(false);
				yearToPicker.setEnabled(false);
				monthToPicker.setEnabled(false);
				selectedDateMap.values().forEach(SelectedYearWrapper::enableChildren);
				addYearButton.setEnabled(true);
			} else {

				yearPicker.setEnabled(false);
				monthPicker.setEnabled(false);
				yearToPicker.setEnabled(false);
				monthToPicker.setEnabled(false);
				selectedDateMap.values().forEach(SelectedYearWrapper::disableChildren);
				addYearButton.setEnabled(false);
			}
		});

		Map<String, List<Customer>> customerPerCategory = customerService.listAllByCustomerTag();
		
		accountNameComboBox = new Select<Customer>();
		accountNameComboBox.setLabel("Outlet/Store Name");
		accountNameComboBox.setItemLabelGenerator(Customer::getOwnerName);
		accountNameComboBox.setWidth("50%");
		for (Entry<String, List<Customer>> entrySet : customerPerCategory.entrySet()) {
			availableCustomers.addAll(entrySet.getValue());
		}
		accountNameComboBox.setItems(availableCustomers);
		for (Entry<String, List<Customer>> entrySet : customerPerCategory.entrySet()) {

			List<Customer> value = entrySet.getValue();
			if (value != null && !value.isEmpty()) {	
				
				Div divider = new Div();
				divider.add(new H5(entrySet.getKey()));
				divider.add(new Hr());

				accountNameComboBox.prependComponents(Iterables.get(value, 0), divider);
			}
			
		}
		accountNameComboBox.addValueChangeListener(e-> {
			accountName.setValue(e.getValue().getOwnerName());
		});
		
		accountName.setLabel("Account Name");
		accountName.setReadOnly(true);
		accountName.setWidth("50%");

		List<AppUser> users = userService.listAll(Sort.unsorted());

		checkedBy = new ComboBox<AppUser>("Checked By");
		checkedBy.setItems(users);
		checkedBy.setItemLabelGenerator(appUser -> PfdiUtil.getFullName(appUser));
		checkedBy.setWidth("50%");

		HorizontalLayout fieldsLayout = new HorizontalLayout();
		fieldsLayout.setWidth("50%");

		approvedBy = new ComboBox<AppUser>("Approved By");
		approvedBy.setItems(users);
		approvedBy.setItemLabelGenerator(appUser -> PfdiUtil.getFullName(appUser));
		approvedBy.setWidth("50%");

		HorizontalLayout accountsLayout = new HorizontalLayout();
		accountsLayout.setWidth("50%");
		accountsLayout.add(accountNameComboBox, accountName);
		
		fieldsLayout.add(checkedBy, approvedBy);

		add(accountsLayout, radioGroup, new Hr(), fieldsLayout);
		setVisible(true);
		setResponsiveSteps(new ResponsiveStep("0", 1));

	}
	
	private class SelectedYearWrapper extends HorizontalLayout {

		private static final long serialVersionUID = 1L;

		Button removeButton;

		private ComboBox<Integer> yearSelectPicker;
		private MultiSelectComboBox<Month> monthSelectPicker;

		protected SelectedYearWrapper(List<Integer> selectableYears) {

			LocalDate currentDate = LocalDate.now();
			yearSelectPicker = new ComboBox<>();
			yearSelectPicker.setItems(selectableYears);
			yearSelectPicker.setPlaceholder("Select Year");
			yearSelectPicker.setEnabled(false);
			yearSelectPicker.setValue(currentDate.getYear());

			monthSelectPicker = new MultiSelectComboBox<>();
			monthSelectPicker.setItems(Month.values());
			monthSelectPicker.setPlaceholder("Select Month");
			monthSelectPicker.setItemLabelGenerator(m -> m.getDisplayName(TextStyle.FULL, Locale.getDefault()));
			monthSelectPicker.setEnabled(false);
			monthSelectPicker.setValue(currentDate.getMonth());
			
			
			removeButton = new Button(new Icon(VaadinIcon.MINUS));
			removeButton.setEnabled(false);
			
			add(yearSelectPicker, monthSelectPicker, removeButton);


		}

		public void enableChildren() {
			setEnabledChildren(true);
			
		}
		
		public void disableChildren() {
			setEnabledChildren(false);
			
		}

		public ComboBox<Integer> getYear() {
			return yearSelectPicker;
		}

		public MultiSelectComboBox<Month> getMonth() {
			return monthSelectPicker;
		}

		public Button getRemoveButton() {
			return removeButton;
		}


		private void setEnabledChildren(boolean enabled) {
			removeButton.setEnabled(enabled);
			yearSelectPicker.setEnabled(enabled);
			monthSelectPicker.setEnabled(enabled);
			
		}
	}

}

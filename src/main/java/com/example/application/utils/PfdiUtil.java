package com.example.application.utils;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import com.example.application.data.OrderStatus;
import com.example.application.data.Role;
import com.example.application.data.entity.AppUser;
import com.example.application.data.entity.products.CustomerTag;
import com.google.gwt.thirdparty.guava.common.collect.Sets;

public class PfdiUtil {

	private final static Set<String> COMPANY_OWNED_TAGS = Sets.newHashSet("Relative Owned", "Company Owned",
			"Main Store");

	public static final NumberFormat getFormatter() {

		NumberFormat currencyInstanceFormat = NumberFormat.getCurrencyInstance();
		currencyInstanceFormat.setMinimumFractionDigits(2);
		currencyInstanceFormat.setMaximumFractionDigits(2);

		return currencyInstanceFormat;
	}

	public static final boolean isChecker(AppUser user) {
		return Role.Checker.name().equalsIgnoreCase(user.getRole());
	}

	public static final boolean isSales(AppUser user) {
		return Role.Sales.name().equalsIgnoreCase(user.getRole());
	}

	public static final boolean isSuperUser(AppUser user) {
		return Role.Superuser.name().equalsIgnoreCase(user.getRole());

	}

	public static final boolean isAdmin(AppUser user) {
		return Role.Admin.name().equalsIgnoreCase(user.getRole());
	}

	public static final boolean isOrderStatusEquals(String currentOrderStatus, OrderStatus orderStatus) {
		return orderStatus.getOrderStatusName().equals(currentOrderStatus);
	}

	
	public static final boolean isRelativeOrCompanyOwned(CustomerTag customerTag) {
		return COMPANY_OWNED_TAGS.contains(customerTag.getCustomerTagName());
	}

	public static final boolean isRelativeOrCompanyOwned(String customerTag) {
		return COMPANY_OWNED_TAGS.contains(customerTag);
	}

	public static final String formatDate(LocalDateTime localDateTime) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		String formatDateTime = localDateTime.format(formatter);

		return formatDateTime;
	}

	public static String formatDateWithHours(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		String formatDateTime = localDateTime.format(formatter);

		return formatDateTime;
	}

}

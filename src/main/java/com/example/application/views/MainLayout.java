package com.example.application.views;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import com.cloudinary.utils.ObjectUtils;
import com.example.application.data.entity.AppUser;
import com.example.application.data.service.UserService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.administration.AdministrationView;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.customer.CustomerView;
import com.example.application.views.products.AddNewProductView;
import com.example.application.views.products.ManageCategoriesView;
import com.example.application.views.products.ManageSizesView;
import com.example.application.views.products.ManageTagsView;
import com.example.application.views.products.ProductsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {


	private static final long serialVersionUID = 3493362210483888466L;
	private AuthenticatedUser authenticatedUser;
	private AccessAnnotationChecker accessChecker;
	private final static int MAX_FILE_SIZE_BYTES = 2000 * 10 * 1024 * 1024; // 10MB
	private UserService userService;
	private final Cloudinary cloudinary = Singleton.getCloudinary();
	private AppUser currentUser;

	public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker, UserService userService) {
		this.authenticatedUser = authenticatedUser;
		this.accessChecker = accessChecker;
		this.userService = userService;

		addToNavbar(createHeaderContent());
	}

	private Component createHeaderContent() {
		Header header = new Header();
		header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "flex-col", "w-full");

		Div layout = new Div();
		layout.addClassNames("flex", "h-xl", "items-center", "px-l", CssClassNamesConstants.MAIN_LAYOUT_HEADER_WRAPPER);

		H2 appName = new H2("PFDI Business Management Application");
		appName.addClassNames("my-0", "me-auto");

		layout.add(appName);

		Optional<AppUser> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			createAvatarDiv(layout, maybeUser);
		} else {
			Anchor loginLink = new Anchor("lUI.getCurrent().getClass().getSimpleName())ogin", "Sign in");
			layout.add(loginLink);
		}

		Nav nav = new Nav();
		nav.addClassNames("gap-s", "overflow-auto", "px-m", CssClassNamesConstants.NAV_BAR_WRAPPER);

		Tabs tabs = getTabs();
		nav.add(tabs);
		header.add(layout, nav);

		return header;
	}

	private void createAvatarDiv(Div layout, Optional<AppUser> maybeUser) {

		AppUser user = maybeUser.get();
		Avatar avatar = new Avatar(user.getFirstName());
		avatar.setWidth("2.5em");
		avatar.setHeight("2.5em");
		avatar.setImage(user.getProfilePictureUrl());

		Button avatarButton = new Button();
		avatarButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
		avatarButton.setIcon(avatar);
		avatarButton.setSizeUndefined();

		Div div = new Div();
		div.setVisible(false);
		div.addClassName(CssClassNamesConstants.USER_PROFILE_WRAPPER);
		div.setHeight("100vh");
		div.setWidth("35vw");
		div.scrollIntoView();

		VerticalLayout nameWrapper = new VerticalLayout();
		nameWrapper.addClassName(CssClassNamesConstants.PROFILE_DETAILS_NAME_AVATAR_WRAPPER);
		nameWrapper.setAlignItems(Alignment.CENTER);

		Avatar profilePicture = new Avatar(user.getFirstName());
		profilePicture.setImage(user.getProfilePictureUrl());
		profilePicture.addClassName("profile-picture");
		
		MemoryBuffer memoryBuffer = new MemoryBuffer();
		
		Upload uploadImage = new Upload();
		uploadImage.setDropAllowed(false);
		uploadImage.setAcceptedFileTypes("image/*");
		uploadImage.setMaxFiles(1);
		uploadImage.setMaxFileSize(MAX_FILE_SIZE_BYTES);
		uploadImage.setReceiver(memoryBuffer);
		uploadImage.addFileRejectedListener(event -> {
			String errorMessage = event.getErrorMessage();

			Notification notification = Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		});
		uploadImage.addSucceededListener(event -> {
			
			InputStream fileData = memoryBuffer.getInputStream();
			String fileName = event.getFileName();	

			try {
				BufferedImage bufferedImage = ImageIO.read(fileData);
				File file = new File(FileUtils.getTempDirectoryPath() + fileName);
				file.createNewFile();
				
				
				if (file.exists()) {
					ImageIO.write(bufferedImage, FilenameUtils.getExtension(fileName), file);
					
					Map<?, ?> uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
					String url = uploadResult.get("url").toString();
					System.out.println(url);
					StreamResource imageResource = new StreamResource("profilePicture",
							() -> memoryBuffer.getInputStream());

					profilePicture.setImageResource(imageResource);
					avatar.setImageResource(imageResource);
					user.setProfilePictureUrl(url);
					cloudinary.uploader().destroy(fileName, uploadResult);
					userService.update(user);
				}
				
				

			} catch (IOException e1) {
				Notification notification = Notification.show("Upload failed. Please try again. If issue persist, please contact system administrator.", 5000, Notification.Position.MIDDLE);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} 
			//processFile(fileData, fileName, contentLength, mimeType);

		});
		
		Button uploadButton = new Button();
		uploadButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
		uploadButton.setIcon(profilePicture);
		uploadButton.setWidth("10em");
		uploadButton.setHeight("10em");

		uploadImage.setUploadButton(uploadButton);
		
		VerticalLayout uploadButtonWrapper = new VerticalLayout();
		uploadButtonWrapper.addClassName(CssClassNamesConstants.PROFILE_DETAILS_NAME_AVATAR_WRAPPER);
		uploadButtonWrapper.setAlignItems(Alignment.CENTER);
		uploadButtonWrapper.add(uploadImage);

		VerticalLayout namePositionWrapper = new VerticalLayout();
		Label fullName = new Label(user.getLastName() + ", " + user.getFirstName());
		Label position = new Label(user.getPosition());
		position.setClassName("bold-label");
		namePositionWrapper.add(fullName, position);
		namePositionWrapper.setAlignItems(Alignment.CENTER);
		namePositionWrapper.setPadding(true);

		nameWrapper.add(uploadButtonWrapper);
		nameWrapper.add(namePositionWrapper);
		nameWrapper.setPadding(false);
		nameWrapper.setSpacing(false);

		VerticalLayout profileDetailsWrapper = new VerticalLayout();
		profileDetailsWrapper.setAlignItems(Alignment.START);
		profileDetailsWrapper.setWidthFull();
		profileDetailsWrapper.setSpacing(false);
		profileDetailsWrapper.setPadding(false);
		profileDetailsWrapper.addClassName(CssClassNamesConstants.PROFILE_DETAILS_WRAPPER);

		Accordion profileAccordion = new Accordion();
		profileAccordion.addClassName(CssClassNamesConstants.PROFILE_DETAILS_ACCORDION);
		profileAccordion.setWidth("100%");

		// ACCPOUNT ID & CONTENT
		HorizontalLayout accountIdWrapper = new HorizontalLayout();
		accountIdWrapper.setSpacing(true);
		accountIdWrapper.setPadding(false);
		Label accountIdLabel = new Label("Account Id: ");
		accountIdLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		Label accountId = new Label(user.getId().toString());
		accountId.addClassName(CssClassNamesConstants.PROFILE_DETAILS_VALUE_WRAPPER);
		accountIdWrapper.add(accountIdLabel, accountId);
		accountIdWrapper.setAlignItems(Alignment.START);

		// LOCATION WRAPPER & CONTENT
		HorizontalLayout locationWrapper = new HorizontalLayout();
		locationWrapper.setSpacing(true);
		locationWrapper.setPadding(false);
		Label locationLabel = new Label("Location: ");
		locationLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		Label location = new Label(user.getLocation().toUpperCase());
		location.addClassName(CssClassNamesConstants.PROFILE_DETAILS_VALUE_WRAPPER);
		locationWrapper.add(locationLabel, location);
		locationWrapper.setAlignItems(Alignment.START);

		// ROLES WRAPPER & CONTENTN
		HorizontalLayout rolesWrapper = new HorizontalLayout();
		rolesWrapper.setSpacing(true);
		rolesWrapper.setPadding(false);
		Label rolesLabel = new Label("Roles: ");
		rolesLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		Label role = new Label(user.getRole());
		role.addClassName(CssClassNamesConstants.PROFILE_DETAILS_VALUE_WRAPPER);
		rolesWrapper.add(rolesLabel, role);
		rolesWrapper.setAlignItems(Alignment.START);

		// Creates layout and adds wrappers to the layout
		VerticalLayout profileDetailsContent = new VerticalLayout(accountIdWrapper, locationWrapper, rolesWrapper);
		profileDetailsContent.setWidth("100%");

		// ACCORDION PANEL FOR Change Password
		VerticalLayout changePasswordContent = new VerticalLayout();
		profileDetailsContent.setWidth("100%");

		FormLayout resetPasswordFormLayout = new FormLayout();

		PasswordField passwordField = new PasswordField();
		passwordField.setLabel("Password");
		passwordField.setHelperText(
				"A password must be at least 8 characters. It has to have at least one letter and one digit.");
		passwordField.setPattern("^(?=.*[0-9])(?=.*[a-zA-Z]).{8}.*$");
		passwordField.setErrorMessage("Not a valid password");
		passwordField.setRequired(true);
		passwordField.setRevealButtonVisible(false);

		PasswordField resetPasswordField = new PasswordField();
		resetPasswordField.setLabel("Confirm Password");
		resetPasswordField.setPattern("^(?=.*[0-9])(?=.*[a-zA-Z]).{8}.*$");
		resetPasswordField.setErrorMessage("Not a valid password");
		resetPasswordField.setRequired(true);
		resetPasswordField.setRevealButtonVisible(false);

		resetPasswordFormLayout.add(passwordField);
		resetPasswordFormLayout.add(resetPasswordField);

		changePasswordContent.add(resetPasswordFormLayout);

		AccordionPanel profileDetailsPanel = profileAccordion.add("Profile Details", profileDetailsContent);
		profileDetailsPanel.setOpened(false);

		AccordionPanel changePasswordPanel = profileAccordion.add("Change Password", changePasswordContent);
		changePasswordPanel.setOpened(false);
		changePasswordContent.addClassName(CssClassNamesConstants.CHANGE_PASSWORD_ACCORDION_PANEL);

		avatarButton.addClickListener(e -> {
			if (!div.isVisible()) {
				div.setVisible(true);
			} else {
				div.setVisible(false);
			}
		});
		profileDetailsWrapper.add(profileAccordion);
		div.add(nameWrapper);
		div.add(profileDetailsWrapper);

		VerticalLayout logoutButtonWrapper = new VerticalLayout();
		logoutButtonWrapper.setAlignItems(Alignment.CENTER);

		Button logout = new Button("Logout");
		logout.addClickListener(e -> openDialog().open());
		logout.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		logoutButtonWrapper.add(logout);

		div.add(logoutButtonWrapper);

		layout.add(div);
		layout.add(avatarButton);
	}

	private ConfirmDialog openDialog() {

		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader("");
		dialog.setText("Are you sure you want to logout?");

		dialog.setCancelable(true);
		dialog.setCancelText("No");
		dialog.setCancelButtonTheme("error primary");
		dialog.addCancelListener(event -> dialog.close());

		dialog.setConfirmText("Yes");
		dialog.setConfirmButtonTheme("primary");
		dialog.addConfirmListener(event -> authenticatedUser.logout());

		return dialog;
	}

	private Tabs getTabs() {
		Tabs tabs = new Tabs();
		tabs.getStyle().set("margin", "auto");


		Tab productsTab = createTab("Product Management", ProductsView.class, "product-view-tab");
		MenuBar menuBar = new MenuBar();
		menuBar.setOpenOnHover(true);
		menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_ICON);
		MenuItem menuItem = menuBar.addItem(new Icon(VaadinIcon.CHEVRON_DOWN_SMALL));
		menuItem.getElement().setAttribute("aria-label", "More options");
		SubMenu subMenu = menuItem.getSubMenu();
		
		RouterLink productList = createNewRoute("Product List", ProductsView.class);
		RouterLink createNewProductPageLink = createNewRoute("Add Product", AddNewProductView.class);
		RouterLink manageTag = createNewRoute("Manage Tags", ManageTagsView.class);
		RouterLink manageSizes = createNewRoute("Manage Sizes", ManageSizesView.class);
		RouterLink manageCategories = createNewRoute("Manage Categories", ManageCategoriesView.class);

		
		subMenu.addItem(productList);
		subMenu.addItem(createNewProductPageLink);
		subMenu.addItem(manageTag);
		subMenu.addItem(manageSizes);
		subMenu.addItem(manageCategories);
		productsTab.add(menuBar);	
		
		Tab profilesTab = createTab("Profile Management", AdministrationView.class, "admin-view-tab");
		Tab customersTab = createTab("Customer Management", CustomerView.class, "admin-view-tab");

		tabs.add(profilesTab, productsTab, customersTab);
		return tabs;
	}

	private RouterLink createNewRoute(String routeText, Class<? extends Component> routeClass) {
		RouterLink manageTag = new RouterLink();
		manageTag.setText(routeText);
		manageTag.setRoute(routeClass);
		return manageTag;
	}

	private Tab createTab(String viewName, Class<? extends Component> viewClass, String id) {
		RouterLink link = new RouterLink();
		link.addClassName(id);
		link.setText(viewName);
		link.setRoute(viewClass);
		return new Tab(link);
	}
	
	

}

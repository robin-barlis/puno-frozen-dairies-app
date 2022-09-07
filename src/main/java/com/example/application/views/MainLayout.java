package com.example.application.views;

import java.util.Optional;

import com.example.application.data.entity.AppUser;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.administration.AdministrationView;
import com.example.application.views.constants.CssClassNamesConstants;
import com.example.application.views.products.ProductsView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private static final long serialVersionUID = 1L;

	/**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private static final long serialVersionUID = 1L;
		private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo class names for various styling
            link.addClassNames("flex", "h-m", "items-center", "px-s", "relative", "text-secondary");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo class names for various styling
            text.addClassNames("font-medium", "text-s", "whitespace-nowrap");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            private static final long serialVersionUID = 1L;

			public LineAwesomeIcon(String lineawesomeClassnames) {
                // Use Lumo classnames for suitable font size and margin
                addClassNames("me-s", "text-l");
                if (!lineawesomeClassnames.isEmpty()) {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }

    }

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        addToNavbar(createHeaderContent());
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "flex-col", "w-full");

        Div layout = new Div();
        layout.addClassNames("flex", "h-xl", "items-center", "px-l", CssClassNamesConstants.MAIN_LAYOUT_HEADER_WRAPPER);

        H2 appName = new H2("Puno Frozen Dairies");
        appName.addClassNames("my-0", "me-auto");
        
        
        layout.add(appName);

        Optional<AppUser> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            createAvatarDiv(layout, maybeUser);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        Nav nav = new Nav();
        nav.addClassNames("flex", "gap-s", "overflow-auto", "px-m", CssClassNamesConstants.NAV_BAR_WRAPPER);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("flex", "list-none", "m-0", "p-0");
        
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }

        }

        header.add(layout, nav);
        return header;
    }

	private void createAvatarDiv(Div layout, Optional<AppUser> maybeUser) {

		AppUser user = maybeUser.get();
		Avatar avatar = new Avatar(user.getFirstName());
		avatar.setWidth("2.5em");
		avatar.setHeight("2.5em");
		
		
		Div div = new Div();
		div.setVisible(false);
		div.addClassName(CssClassNamesConstants.USER_PROFILE_WRAPPER);
		div.setHeight("100vh");
		div.setWidth("35vw");		
		
		VerticalLayout nameWrapper = new VerticalLayout();
		nameWrapper.addClassName(CssClassNamesConstants.PROFILE_DETAILS_NAME_AVATAR_WRAPPER);
		nameWrapper.setAlignItems(Alignment.CENTER);
		
		Avatar profilePicture = new Avatar(user.getFirstName());
		profilePicture.setWidth("7em");
		profilePicture.setHeight("7em");
		
		VerticalLayout namePositionWrapper = new VerticalLayout();		
		Label fullName = new Label(user.getLastName() + ", " + user.getFirstName());
		Label position = new Label(user.getPosition());
		position.setClassName("bold-label");		
		namePositionWrapper.add(fullName, position);
		namePositionWrapper.setAlignItems(Alignment.CENTER);
		namePositionWrapper.setPadding(true);

		nameWrapper.add(profilePicture);
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
			
		//ROLES WRAPPER & CONTENTN
		HorizontalLayout rolesWrapper = new HorizontalLayout();
		rolesWrapper.setSpacing(true);
		rolesWrapper.setPadding(false);	
		Label rolesLabel = new Label("Roles: ");
		rolesLabel.addClassName(CssClassNamesConstants.PROFILE_DETAILS_LABEL_WRAPPER);
		Label role = new Label(String.join(", ", user.getRoles()));
		role.addClassName(CssClassNamesConstants.PROFILE_DETAILS_VALUE_WRAPPER);
		rolesWrapper.add(rolesLabel, role);
		rolesWrapper.setAlignItems(Alignment.START);	
		
		// Creates layout and adds wrappers to the layout
		VerticalLayout profileDetailsContent = new VerticalLayout(accountIdWrapper,locationWrapper, rolesWrapper);
		profileDetailsContent.setWidth("100%");
		
		
		// ACCORDION PANEL FOR Change Password
		VerticalLayout changePasswordContent = new VerticalLayout();
		profileDetailsContent.setWidth("100%");
		
		FormLayout resetPasswordFormLayout = new FormLayout();
		
		PasswordField passwordField = new PasswordField();
		passwordField.setLabel("Password");
		passwordField.setHelperText("A password must be at least 8 characters. It has to have at least one letter and one digit.");
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

		
		profileDetailsWrapper.add(profileAccordion);		
		div.add(nameWrapper);
		div.add(profileDetailsWrapper);

		
		ComponentUtil.addListener(avatar, ClickEvent.class, e-> {
			if (!div.isVisible()) {
		    	div.setVisible(true);
			} else {
				div.setVisible(false);
			}
		
		});

		layout.add(div);
		layout.add(avatar);
	}

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("Profile Management", "la", AdministrationView.class), //

                new MenuItemInfo("Product Management", "la", ProductsView.class), //

        };
    }

}

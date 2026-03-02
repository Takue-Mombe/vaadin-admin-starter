package com.adminpro.ui.layout;

import com.adminpro.domain.User;
import com.adminpro.security.CurrentUserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteConfiguration;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Map;
/**
 * Application shell that wraps every view.
 *
 * Structure:
 *   AppLayout
 *   ├── Navbar  → TopBar (title + toggle + avatar)
 *   └── Drawer  → SideNav (brand + nav items + footer)
 *
 * Every view declares {@code @Route(layout = MainLayout.class)}.
 * The @Theme annotation lives in AppShell (Vaadin 24 requirement).
 */
@PermitAll
public class MainLayout extends AppLayout {

    private static final Map<String, String> PAGE_TITLES = Map.ofEntries(
        Map.entry("", "Dashboard"),
        Map.entry("dashboard", "Dashboard"),
        Map.entry("analytics", "Analytics"),
        Map.entry("users", "Users"),
        Map.entry("orders", "Orders"),
        Map.entry("products", "Products"),
        Map.entry("messages", "Messages"),
        Map.entry("reports", "Reports"),
        Map.entry("settings", "Settings")
    );

    private final Span currentPage = new Span("Dashboard");
    private final Button themeToggle = new Button();
    private final AuthenticationContext authenticationContext;
    private final String currentUserName;
    private final String currentUserRole;
    private final String currentUserInitials;
    private boolean darkTheme;

    public MainLayout(CurrentUserService currentUserService,
                      AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        User currentUser = currentUserService.getCurrentUser().orElse(null);
        currentUserName = currentUser == null ? "Admin User" : currentUser.getFullName();
        currentUserRole = currentUser == null ? "User" : formatRole(currentUser.getRole().name());
        currentUserInitials = computeInitials(currentUserName);

        setPrimarySection(Section.DRAWER);

        addToDrawer(new SideNav(currentUserName, currentUserRole));
        addToNavbar(buildNavbar());

        getElement().executeJs("document.documentElement.setAttribute('data-theme', 'light')");
    }

    private Div buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("topbar__toggle");

        Span rootCrumb = new Span("Arc");
        rootCrumb.addClassName("crumb-item");

        Span separator = new Span(".");
        separator.addClassName("crumb-sep");

        currentPage.addClassNames("crumb-item", "active");

        Div breadcrumb = new Div(rootCrumb, separator, currentPage);
        breadcrumb.addClassName("topbar__crumb");

        Div left = new Div(toggle, breadcrumb);
        left.addClassName("topbar__left");

        TextField search = new TextField();
        search.addClassName("topbar__search");
        search.setPlaceholder("Search...");
        search.setClearButtonVisible(true);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setWidth("220px");

        themeToggle.addClassName("icon-btn");
        themeToggle.getElement().setAttribute("aria-label", "Toggle theme");
        setThemeIcon();
        themeToggle.addClickListener(event -> toggleTheme());

        Button notificationButton = new Button(VaadinIcon.BELL_O.create());
        notificationButton.addClassNames("icon-btn", "icon-btn--notify");
        notificationButton.getElement().setAttribute("aria-label", "Notifications");

        ContextMenu notificationMenu = new ContextMenu(notificationButton);
        notificationMenu.setOpenOnClick(true);
        notificationMenu.addItem("New user Sarah Kim registered");
        notificationMenu.addItem("Revenue milestone reached: $240K");
        notificationMenu.addItem("Acme Corp billing failed");
        notificationMenu.addItem("Monthly report for February is ready");

        Div avatarBadge = new Div();
        avatarBadge.addClassName("avatar-badge");
        avatarBadge.setText(currentUserInitials);

        Span avatarName = new Span(currentUserName);
        avatarName.addClassName("avatar-name");

        Div avatarTrigger = new Div(avatarBadge, avatarName, VaadinIcon.ANGLE_DOWN.create());
        avatarTrigger.addClassName("avatar-trigger");

        ContextMenu avatarMenu = new ContextMenu(avatarTrigger);
        avatarMenu.setOpenOnClick(true);
        avatarMenu.addItem("Profile");
        avatarMenu.addItem("Settings", event -> getUI().ifPresent(ui -> ui.navigate("settings")));
        avatarMenu.addItem("Sign out", event -> authenticationContext.logout());

        Div actions = new Div(search, themeToggle, notificationButton, avatarTrigger);
        actions.addClassName("topbar__actions");

        Div navbar = new Div(left, actions);
        navbar.addClassName("topbar");

        return navbar;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        currentPage.setText(resolvePageTitle());
    }

    private String resolvePageTitle() {
        Component content = getContent();
        if (content == null) {
            return "Dashboard";
        }

        String path = RouteConfiguration.forSessionScope().getUrl(content.getClass());
        if (path == null || path.isBlank()) {
            return "Dashboard";
        }

        String rootPath = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
        return PAGE_TITLES.getOrDefault(rootPath, capitalize(rootPath));
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Dashboard";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        getElement().executeJs(
            "document.documentElement.setAttribute('data-theme', $0)",
            darkTheme ? "dark" : "light"
        );
        setThemeIcon();
    }

    private void setThemeIcon() {
        themeToggle.setIcon(darkTheme ? VaadinIcon.MOON_O.create() : VaadinIcon.SUN_O.create());
    }

    private String computeInitials(String value) {
        String[] parts = value == null ? new String[0] : value.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "AU";
        }

        String first = parts[0].substring(0, 1).toUpperCase();
        String second = parts.length > 1 && !parts[parts.length - 1].isBlank()
            ? parts[parts.length - 1].substring(0, 1).toUpperCase()
            : "X";
        return first + second;
    }

    private String formatRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "User";
        }

        String normalized = roleName.toLowerCase().replace('_', ' ');
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

    /**
     * Views can call this helper to update the topbar title after navigation.
     * Called automatically by each view in its constructor.
     */
    public void setPageTitle(String title) {
        currentPage.setText(title);
    }
}

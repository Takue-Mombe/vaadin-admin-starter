package com.adminpro.ui.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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
public class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(new SideNav());
        addToNavbar(buildNavbar());
    }

    private Div buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getStyle()
              .set("margin-right", "var(--lumo-space-s)")
              .set("color", "var(--lumo-contrast-60pct)");

        Span pageTitle = new Span();
        pageTitle.addClassName("topbar__title");
        pageTitle.setId("page-title");

        Div avatarEl = new Div();
        avatarEl.addClassName("topbar__avatar");
        avatarEl.setText("AU");
        avatarEl.getElement().setAttribute("title", "Admin User");

        Div actions = new Div(avatarEl);
        actions.addClassName("topbar__actions");

        Div navbar = new Div(toggle, pageTitle, actions);
        navbar.addClassName("topbar");
        navbar.getStyle().set("width", "100%");

        return navbar;
    }

    /**
     * Views can call this helper to update the topbar title after navigation.
     * Called automatically by each view in its constructor.
     */
    public void setPageTitle(String title) {
        getElement().executeJs(
            "const el = document.getElementById('page-title'); if(el) el.textContent = $0;",
            title
        );
    }
}

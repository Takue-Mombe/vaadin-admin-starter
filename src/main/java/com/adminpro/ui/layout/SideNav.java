package com.adminpro.ui.layout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;

/**
 * Sidebar navigation drawer content.
 *
 * Renders:
 *   - Brand logo block
 *   - "Main Menu" section with nav links
 *   - Footer with user identity
 *
 * To add a new page, add one {@link NavEntry} to the NAV_ITEMS list and
 * create the corresponding view class – that is the only change required.
 */
public class SideNav extends Div {

    // ── Nav registry – single source of truth for routing + icons ─────────
    private record NavEntry(String label, Class<? extends com.vaadin.flow.component.Component> viewClass, VaadinIcon icon) {}

    // Views are forward-referenced; imports resolved at build time.
    private static final java.util.List<NavEntry> NAV_ITEMS = java.util.List.of(
        new NavEntry("Dashboard", com.adminpro.ui.views.dashboard.DashboardView.class, VaadinIcon.DASHBOARD),
        new NavEntry("Users",     com.adminpro.ui.views.users.UsersView.class,         VaadinIcon.USERS),
        new NavEntry("Settings",  com.adminpro.ui.views.settings.SettingsView.class,   VaadinIcon.COG)
    );

    public SideNav() {
        getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("height", "100%");

        add(buildBrand());
        add(buildNavSection());
        add(buildFooter());
    }

    // ── Brand / logo ──────────────────────────────────────────────────────

    private Div buildBrand() {
        Div iconBox = new Div();
        iconBox.addClassName("sidebar-brand__icon");
        iconBox.setText("AP");

        Div nameBlock = new Div();
        Span name = new Span("AdminPro");
        name.addClassName("sidebar-brand__name");
        Span tagline = new Span("Admin Kit");
        tagline.addClassName("sidebar-brand__tagline");
        nameBlock.add(name, new Div(tagline));

        Div brand = new Div(iconBox, nameBlock);
        brand.addClassName("sidebar-brand");
        return brand;
    }

    // ── Nav links ─────────────────────────────────────────────────────────

    private Div buildNavSection() {
        Div section = new Div();
        section.getStyle().set("flex", "1");

        Span sectionLabel = new Span("Main Menu");
        sectionLabel.addClassName("nav-section-label");
        section.add(sectionLabel);

        for (NavEntry entry : NAV_ITEMS) {
            section.add(buildNavItem(entry));
        }

        return section;
    }

    private RouterLink buildNavItem(NavEntry entry) {
        Div iconWrapper = new Div(entry.icon().create());
        iconWrapper.getStyle().set("display", "flex").set("align-items", "center");

        Span label = new Span(entry.label());

        RouterLink link = new RouterLink();
        link.setRoute(entry.viewClass());
        link.addClassName("nav-item");
        link.add(iconWrapper, label);

        // Highlight active link via Vaadin's built-in RouterLink highlight
        link.setHighlightCondition((router, event) ->
            event.getLocation().getPath().startsWith(
                router.getHref().replaceFirst("^/", "")
            )
        );
        link.setHighlightAction((anchor, highlight) -> {
            if (highlight) {
                anchor.addClassName("active");
            } else {
                anchor.removeClassName("active");
            }
        });

        return link;
    }

    // ── Footer (current user) ─────────────────────────────────────────────

    private Div buildFooter() {
        Div avatar = new Div();
        avatar.addClassName("sidebar-footer__avatar");
        avatar.setText("AU");

        Span name    = new Span("Admin User");
        name.addClassName("sidebar-footer__name");
        Span role    = new Span("Administrator");
        role.addClassName("sidebar-footer__role");

        Div nameBlock = new Div(name, role);
        nameBlock.getStyle().set("display", "flex").set("flex-direction", "column").set("min-width", "0");

        Div footer = new Div(avatar, nameBlock);
        footer.addClassName("sidebar-footer");
        return footer;
    }
}

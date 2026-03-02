package com.adminpro.ui.layout;

import com.adminpro.ui.views.analytics.AnalyticsView;
import com.adminpro.ui.views.dashboard.DashboardView;
import com.adminpro.ui.views.messages.MessagesView;
import com.adminpro.ui.views.orders.OrdersView;
import com.adminpro.ui.views.products.ProductsView;
import com.adminpro.ui.views.reports.ReportsView;
import com.adminpro.ui.views.settings.SettingsView;
import com.adminpro.ui.views.users.UsersView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final String userName;
    private final String userRole;
    private final String userInitials;

    // ── Nav registry – single source of truth for routing + icons ─────────
    private record NavEntry(
            String section,
            String label,
            Class<? extends com.vaadin.flow.component.Component> viewClass,
            VaadinIcon icon,
            String pip
    ) {}

    // Views are forward-referenced; imports resolved at build time.
    private static final List<NavEntry> NAV_ITEMS = List.of(
        new NavEntry("Workspace", "Dashboard", DashboardView.class, VaadinIcon.DASHBOARD, null),
        new NavEntry("Workspace", "Analytics", AnalyticsView.class, VaadinIcon.TRENDING_UP, "New"),
        new NavEntry("Workspace", "Users", UsersView.class, VaadinIcon.USERS, null),
        new NavEntry("Workspace", "Orders", OrdersView.class, VaadinIcon.CART_O, "14"),
        new NavEntry("Manage", "Products", ProductsView.class, VaadinIcon.DESKTOP, null),
        new NavEntry("Manage", "Messages", MessagesView.class, VaadinIcon.CHAT, "3"),
        new NavEntry("Manage", "Reports", ReportsView.class, VaadinIcon.FILE_TEXT_O, null),
        new NavEntry("System", "Settings", SettingsView.class, VaadinIcon.COG, null)
    );

    public SideNav() {
        this("Admin User", "User");
    }

    public SideNav(String userName, String userRole) {
        this.userName = userName == null || userName.isBlank() ? "Admin User" : userName;
        this.userRole = userRole == null || userRole.isBlank() ? "User" : userRole;
        this.userInitials = computeInitials(this.userName);

        addClassName("arc-sidebar");
        setHeightFull();

        add(buildBrand());
        add(buildNavSection());
        add(buildFooter());
    }

    // ── Brand / logo ──────────────────────────────────────────────────────

    private Div buildBrand() {
        Div iconBox = new Div();
        iconBox.addClassName("sidebar-brand__icon");
        iconBox.setText("A");

        Div nameBlock = new Div();
        Span name = new Span("Arc");
        name.addClassName("sidebar-brand__name");
        Span tagline = new Span("Admin");
        tagline.addClassName("sidebar-brand__tagline");
        nameBlock.add(name, tagline);

        Div brand = new Div(iconBox, nameBlock);
        brand.addClassName("sidebar-brand");
        return brand;
    }

    // ── Nav links ─────────────────────────────────────────────────────────

    private Div buildNavSection() {
        Div body = new Div();
        body.addClassName("nav-body");

        Map<String, List<NavEntry>> bySection = NAV_ITEMS.stream()
            .collect(Collectors.groupingBy(NavEntry::section, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<NavEntry>> sectionEntry : bySection.entrySet()) {
            Div group = new Div();
            group.addClassName("nav-group");

            Span sectionLabel = new Span(sectionEntry.getKey());
            sectionLabel.addClassName("nav-section-label");
            group.add(sectionLabel);

            for (NavEntry item : sectionEntry.getValue()) {
                group.add(buildNavItem(item));
            }

            body.add(group);
        }

        return body;
    }

    private RouterLink buildNavItem(NavEntry entry) {
        Div iconWrapper = new Div(entry.icon().create());
        iconWrapper.addClassName("nav-link__icon");

        Span label = new Span(entry.label());
        label.addClassName("nav-link__text");

        RouterLink link = new RouterLink();
        link.setRoute(entry.viewClass());
        link.addClassName("nav-item");
        link.add(iconWrapper, label);

        if (entry.pip() != null && !entry.pip().isBlank()) {
            Span pip = new Span(entry.pip());
            pip.addClassName("nav-pip");
            link.add(pip);
        }

        // Highlight active link via Vaadin's built-in RouterLink highlight
        link.setHighlightCondition((router, event) ->
            isLinkActive(router.getHref(), event.getLocation().getPath())
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

    private boolean isLinkActive(String href, String path) {
        String cleanHref = href == null ? "" : href.replaceFirst("^/", "");
        String cleanPath = path == null ? "" : path;

        if (cleanHref.isBlank()) {
            return cleanPath.isBlank() || "dashboard".equals(cleanPath);
        }

        return cleanPath.equals(cleanHref) || cleanPath.startsWith(cleanHref + "/");
    }

    // ── Footer (current user) ─────────────────────────────────────────────

    private Div buildFooter() {
        Div avatar = new Div();
        avatar.addClassName("sidebar-footer__avatar");
        avatar.setText(userInitials);

        Span name    = new Span(userName);
        name.addClassName("sidebar-footer__name");
        Span role    = new Span(userRole);
        role.addClassName("sidebar-footer__role");

        Div nameBlock = new Div(name, role);
        nameBlock.getStyle().set("display", "flex").set("flex-direction", "column").set("min-width", "0");

        Div footer = new Div(avatar, nameBlock);
        footer.addClassName("sidebar-footer");
        return footer;
    }

    private String computeInitials(String value) {
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 0) {
            return "AU";
        }

        String first = parts[0].substring(0, 1).toUpperCase();
        String second = parts.length > 1
            ? parts[parts.length - 1].substring(0, 1).toUpperCase()
            : "X";
        return first + second;
    }
}

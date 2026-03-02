package com.adminpro.ui.views.dashboard;

import com.adminpro.application.DashboardService;
import com.adminpro.domain.DashboardStat;
import com.adminpro.domain.User;
import com.adminpro.ui.components.ContentCard;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.components.StatCard;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard — landing page of the admin kit.
 *
 * Sections:
 *   1. Stat cards row  (total / active / admin / manager counts)
 *   2. Recent Activity grid (5 most recently created users)
 */
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | AdminPro")
public class DashboardView extends Div {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    public DashboardView(DashboardService dashboardService) {
        addClassName("page-content");
        setWidthFull();

        add(new PageHeader("Dashboard", "Welcome back! Here's what's happening."));
        add(buildStatsRow(dashboardService.getStats()));
        add(buildRecentActivity(dashboardService.getRecentActivity()));
    }

    // ── Stat cards ────────────────────────────────────────────────────────

    private Div buildStatsRow(List<DashboardStat> stats) {
        Div row = new Div();
        row.addClassName("stats-row");
        stats.stream().map(StatCard::new).forEach(row::add);
        return row;
    }

    // ── Recent activity grid ──────────────────────────────────────────────

    private ContentCard buildRecentActivity(List<User> users) {
        ContentCard card = new ContentCard("Recent Activity", "Last 5 accounts added to the system");
        card.fullWidth();

        Grid<User> grid = new Grid<>(User.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);

        grid.addColumn(User::getFullName)
            .setHeader("Name")
            .setSortable(true)
            .setFlexGrow(2);

        grid.addColumn(User::getEmail)
            .setHeader("Email")
            .setSortable(true)
            .setFlexGrow(3);

        grid.addColumn(u -> u.getRole().name())
            .setHeader("Role")
            .setSortable(true)
            .setFlexGrow(1);

        grid.addComponentColumn(u -> {
            Span badge = new Span(u.isActive() ? "Active" : "Inactive");
            badge.addClassNames("badge", u.isActive() ? "badge--success" : "badge--error");
            return badge;
        }).setHeader("Status").setFlexGrow(1);

        grid.addColumn(u -> u.getCreatedAt().format(DATE_FMT))
            .setHeader("Joined")
            .setSortable(true)
            .setFlexGrow(2);

        grid.setItems(users);
        card.addContent(grid);
        return card;
    }
}

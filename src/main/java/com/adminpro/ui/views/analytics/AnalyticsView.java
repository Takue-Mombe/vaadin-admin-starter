package com.adminpro.ui.views.analytics;

import com.adminpro.application.AnalyticsService;
import com.adminpro.domain.AnalyticsMetric;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Route(value = "analytics", layout = MainLayout.class)
@PageTitle("Analytics | Arc Admin")
@PermitAll
public class AnalyticsView extends Div {

    private static final List<String> TREND_OPTIONS = List.of("UP", "DOWN", "NEUTRAL");
    private static final List<String> UNIT_OPTIONS = List.of("K", "%", "min", "s", "$");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    private final AnalyticsService analyticsService;

    private final Grid<AnalyticsMetric> grid = new Grid<>(AnalyticsMetric.class, false);
    private final TextField searchField = new TextField();
    private final Select<String> trendFilter = new Select<>();

    private final Span totalMetricsValue = new Span("0");
    private final Span upwardTrendsValue = new Span("0");
    private final Span updatedTodayValue = new Span("0");

    public AnalyticsView(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Analytics", "Track and maintain platform KPI metrics");

        Button exportButton = new Button("Export CSV");
        exportButton.addClassNames("btn", "btn-outline");

        Button addMetricButton = new Button("Add Metric", VaadinIcon.PLUS.create());
        addMetricButton.addClassNames("btn", "btn-primary");
        addMetricButton.addClickListener(event -> openDialog(new AnalyticsMetric()));

        header.addAction(exportButton);
        header.addAction(addMetricButton);

        add(header);
        add(buildStats());
        add(buildToolbar());
        add(buildGridCard());

        refreshSummary();
        refreshGrid();
    }

    private Div buildStats() {
        Div stats = new Div();
        stats.addClassNames("stats", "stats--three");
        stats.add(buildStat("Tracked Metrics", totalMetricsValue, "active dashboard KPIs", "neu"));
        stats.add(buildStat("Upward Trends", upwardTrendsValue, "positive direction", "up"));
        stats.add(buildStat("Updated Today", updatedTodayValue, "latest refreshes", "up"));
        return stats;
    }

    private Div buildStat(String label, Span value, String delta, String tone) {
        Div stat = new Div();
        stat.addClassName("stat");

        Span labelEl = new Span(label);
        labelEl.addClassName("stat-label");

        value.addClassName("stat-val");

        Span deltaEl = new Span(delta);
        deltaEl.addClassNames("stat-delta", tone);

        stat.add(labelEl, value, deltaEl);
        return stat;
    }

    private Div buildToolbar() {
        Div card = new Div();
        card.addClassNames("card", "toolbar-card");

        Div body = new Div();
        body.addClassNames("card-b", "toolbar-row");

        searchField.setPlaceholder("Search metric name or period");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("toolbar-search");
        searchField.addValueChangeListener(event -> refreshGrid());

        trendFilter.setLabel("Trend");
        trendFilter.setItems(Arrays.asList("ALL", "UP", "DOWN", "NEUTRAL"));
        trendFilter.setValue("ALL");
        trendFilter.addClassName("toolbar-select");
        trendFilter.addValueChangeListener(event -> refreshGrid());

        body.add(searchField, trendFilter);
        card.add(body);
        return card;
    }

    private Div buildGridCard() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("table-grid");
        grid.setWidthFull();

        grid.addColumn(AnalyticsMetric::getMetricName).setHeader("Metric").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(metric -> formatMetricValue(metric.getMetricValue(), metric.getUnit()))
            .setHeader("Value")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(AnalyticsMetric::getPeriod).setHeader("Period").setAutoWidth(true);
        grid.addComponentColumn(metric -> buildTrendBadge(metric.getTrend())).setHeader("Trend").setAutoWidth(true);
        grid.addColumn(metric -> metric.getUpdatedAt().format(DATE_TIME_FMT)).setHeader("Updated").setAutoWidth(true);
        grid.addComponentColumn(this::buildRowActions).setHeader("").setAutoWidth(true).setFlexGrow(0);

        Div card = new Div();
        card.addClassName("card");

        Div body = new Div();
        body.addClassName("card-b");
        body.add(grid);

        card.add(body);
        return card;
    }

    private String formatMetricValue(BigDecimal value, String unit) {
        String normalized = value == null ? "0" : value.stripTrailingZeros().toPlainString();
        String finalUnit = unit == null || unit.isBlank() ? "" : " " + unit;
        return normalized + finalUnit;
    }

    private Span buildTrendBadge(String trend) {
        Span badge = new Span(trend);
        badge.addClassName("status-pill");

        if ("UP".equalsIgnoreCase(trend)) {
            badge.addClassName("status-pill--green");
        } else if ("DOWN".equalsIgnoreCase(trend)) {
            badge.addClassName("status-pill--red");
        } else {
            badge.addClassName("status-pill--gray");
        }
        return badge;
    }

    private Div buildRowActions(AnalyticsMetric metric) {
        Div actions = new Div();
        actions.addClassName("table-actions");

        Button edit = new Button("Manage");
        edit.addClassNames("btn", "btn-outline", "btn-sm");
        edit.addClickListener(event -> openDialog(metric));

        Button delete = new Button("Delete");
        delete.addClassNames("btn", "btn-danger", "btn-sm");
        delete.addClickListener(event -> confirmDelete(metric));

        actions.add(edit, delete);
        return actions;
    }

    private void openDialog(AnalyticsMetric metric) {
        boolean isNew = metric.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add Metric" : "Edit Metric");
        dialog.setWidth("520px");
        dialog.setCloseOnOutsideClick(false);

        TextField metricName = new TextField("Metric Name");
        metricName.setWidthFull();

        BigDecimalField metricValue = new BigDecimalField("Metric Value");
        metricValue.setWidthFull();

        Select<String> unit = new Select<>();
        unit.setLabel("Unit");
        unit.setItems(UNIT_OPTIONS);
        unit.setWidthFull();

        TextField period = new TextField("Period");
        period.setWidthFull();

        Select<String> trend = new Select<>();
        trend.setLabel("Trend");
        trend.setItems(TREND_OPTIONS);
        trend.setWidthFull();

        Div form = new Div(metricName, metricValue, unit, period, trend);
        form.getStyle().set("display", "grid").set("gap", "var(--lumo-space-s)");

        BeanValidationBinder<AnalyticsMetric> binder = new BeanValidationBinder<>(AnalyticsMetric.class);
        binder.forField(metricName).asRequired().bind(AnalyticsMetric::getMetricName, AnalyticsMetric::setMetricName);
        binder.forField(metricValue).asRequired().bind(AnalyticsMetric::getMetricValue, AnalyticsMetric::setMetricValue);
        binder.forField(unit).bind(AnalyticsMetric::getUnit, AnalyticsMetric::setUnit);
        binder.forField(period).asRequired().bind(AnalyticsMetric::getPeriod, AnalyticsMetric::setPeriod);
        binder.forField(trend).asRequired().bind(AnalyticsMetric::getTrend, AnalyticsMetric::setTrend);
        binder.readBean(metric);

        if (unit.getValue() == null) {
            unit.setValue("K");
        }
        if (trend.getValue() == null) {
            trend.setValue("UP");
        }

        Button save = new Button(isNew ? "Create" : "Save");
        save.addClassNames("btn", "btn-primary");
        save.addClickListener(event -> {
            try {
                binder.writeBean(metric);
                analyticsService.save(metric);
                dialog.close();
                refreshSummary();
                refreshGrid();
                notify(isNew ? "Metric created." : "Metric updated.", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addClassNames("btn", "btn-outline");

        Div footer = new Div(cancel, save);
        footer.addClassName("dialog-footer");

        dialog.add(form);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    private void confirmDelete(AnalyticsMetric metric) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete " + metric.getMetricName() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            analyticsService.delete(metric);
            refreshSummary();
            refreshGrid();
            notify("Metric deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    private void refreshGrid() {
        List<AnalyticsMetric> metrics = analyticsService.search(searchField.getValue());
        if (!"ALL".equalsIgnoreCase(trendFilter.getValue())) {
            metrics = metrics.stream()
                .filter(metric -> trendFilter.getValue().equalsIgnoreCase(metric.getTrend()))
                .toList();
        }
        grid.setItems(metrics);
    }

    private void refreshSummary() {
        List<AnalyticsMetric> metrics = analyticsService.findAll();
        long updatedToday = metrics.stream()
            .filter(metric -> metric.getUpdatedAt() != null && metric.getUpdatedAt().toLocalDate().equals(LocalDate.now()))
            .count();

        totalMetricsValue.setText(String.valueOf(metrics.size()));
        upwardTrendsValue.setText(String.valueOf(analyticsService.countByTrend("UP")));
        updatedTodayValue.setText(String.valueOf(updatedToday));
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}

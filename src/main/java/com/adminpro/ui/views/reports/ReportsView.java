package com.adminpro.ui.views.reports;

import com.adminpro.application.ReportService;
import com.adminpro.domain.ReportConfig;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Route(value = "reports", layout = MainLayout.class)
@PageTitle("Reports | Arc Admin")
@PermitAll
public class ReportsView extends Div {

    private static final List<String> STATUS_OPTIONS = List.of("READY", "RUNNING", "FAILED");
    private static final List<String> CADENCE_OPTIONS = List.of("Daily", "Weekly", "Monthly", "Quarterly");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final ReportService reportService;

    private final Grid<ReportConfig> grid = new Grid<>(ReportConfig.class, false);
    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    private final Span totalReportsValue = new Span("0");
    private final Span readyValue = new Span("0");
    private final Span failedValue = new Span("0");

    public ReportsView(ReportService reportService) {
        this.reportService = reportService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Reports", "Manage generated and scheduled reports");

        Button generateButton = new Button("Generate", VaadinIcon.REFRESH.create());
        generateButton.addClassNames("btn", "btn-outline");
        generateButton.addClickListener(event -> {
            reportService.findAll().stream().findFirst().ifPresent(report -> {
                report.setStatus("RUNNING");
                report.setLastGenerated(LocalDate.now());
                reportService.save(report);
                refreshSummary();
                refreshGrid();
                notify("Report generation started.", NotificationVariant.LUMO_SUCCESS);
            });
        });

        Button addButton = new Button("Add Report", VaadinIcon.PLUS.create());
        addButton.addClassNames("btn", "btn-primary");
        addButton.addClickListener(event -> openDialog(new ReportConfig()));

        header.addAction(generateButton);
        header.addAction(addButton);

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
        stats.add(buildStat("Reports", totalReportsValue, "configured pipelines", "neu"));
        stats.add(buildStat("Ready", readyValue, "available to download", "up"));
        stats.add(buildStat("Failed", failedValue, "needs retry", "dn"));
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

        searchField.setPlaceholder("Search report name, owner, or cadence");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("toolbar-search");
        searchField.addValueChangeListener(event -> refreshGrid());

        statusFilter.setLabel("Status");
        statusFilter.setItems(Arrays.asList("ALL", "READY", "RUNNING", "FAILED"));
        statusFilter.setValue("ALL");
        statusFilter.addClassName("toolbar-select");
        statusFilter.addValueChangeListener(event -> refreshGrid());

        body.add(searchField, statusFilter);
        card.add(body);
        return card;
    }

    private Div buildGridCard() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("table-grid");
        grid.setWidthFull();

        grid.addColumn(ReportConfig::getReportName).setHeader("Report").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(ReportConfig::getOwnerName).setHeader("Owner").setAutoWidth(true);
        grid.addColumn(ReportConfig::getCadence).setHeader("Cadence").setAutoWidth(true);
        grid.addComponentColumn(report -> buildStatusBadge(report.getStatus())).setHeader("Status").setAutoWidth(true);
        grid.addColumn(report -> report.getLastGenerated().format(DATE_FMT)).setHeader("Last Generated").setAutoWidth(true);
        grid.addComponentColumn(this::buildRowActions).setHeader("").setAutoWidth(true).setFlexGrow(0);

        Div card = new Div();
        card.addClassName("card");

        Div body = new Div();
        body.addClassName("card-b");
        body.add(grid);

        card.add(body);
        return card;
    }

    private Span buildStatusBadge(String status) {
        Span badge = new Span(status);
        badge.addClassName("status-pill");

        if ("READY".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--green");
        } else if ("RUNNING".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--amber");
        } else {
            badge.addClassName("status-pill--red");
        }
        return badge;
    }

    private Div buildRowActions(ReportConfig report) {
        Div actions = new Div();
        actions.addClassName("table-actions");

        Button manage = new Button("Manage");
        manage.addClassNames("btn", "btn-outline", "btn-sm");
        manage.addClickListener(event -> openDialog(report));

        Button delete = new Button("Delete");
        delete.addClassNames("btn", "btn-danger", "btn-sm");
        delete.addClickListener(event -> confirmDelete(report));

        actions.add(manage, delete);
        return actions;
    }

    private void openDialog(ReportConfig report) {
        boolean isNew = report.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add Report" : "Edit Report");
        dialog.setWidth("560px");
        dialog.setCloseOnOutsideClick(false);

        TextField name = new TextField("Report Name");
        name.setWidthFull();

        TextField owner = new TextField("Owner");
        owner.setWidthFull();

        Select<String> cadence = new Select<>();
        cadence.setLabel("Cadence");
        cadence.setItems(CADENCE_OPTIONS);
        cadence.setWidthFull();

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems(STATUS_OPTIONS);
        status.setWidthFull();

        DatePicker lastGenerated = new DatePicker("Last Generated");
        lastGenerated.setWidthFull();

        Div form = new Div(name, owner, cadence, status, lastGenerated);
        form.getStyle().set("display", "grid").set("gap", "var(--lumo-space-s)");

        BeanValidationBinder<ReportConfig> binder = new BeanValidationBinder<>(ReportConfig.class);
        binder.forField(name).asRequired().bind(ReportConfig::getReportName, ReportConfig::setReportName);
        binder.forField(owner).asRequired().bind(ReportConfig::getOwnerName, ReportConfig::setOwnerName);
        binder.forField(cadence).asRequired().bind(ReportConfig::getCadence, ReportConfig::setCadence);
        binder.forField(status).asRequired().bind(ReportConfig::getStatus, ReportConfig::setStatus);
        binder.forField(lastGenerated).asRequired().bind(ReportConfig::getLastGenerated, ReportConfig::setLastGenerated);
        binder.readBean(report);

        if (cadence.getValue() == null) {
            cadence.setValue("Monthly");
        }
        if (status.getValue() == null) {
            status.setValue("READY");
        }
        if (lastGenerated.getValue() == null) {
            lastGenerated.setValue(LocalDate.now());
        }

        Button save = new Button(isNew ? "Create" : "Save");
        save.addClassNames("btn", "btn-primary");
        save.addClickListener(event -> {
            try {
                binder.writeBean(report);
                reportService.save(report);
                dialog.close();
                refreshSummary();
                refreshGrid();
                notify(isNew ? "Report created." : "Report updated.", NotificationVariant.LUMO_SUCCESS);
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

    private void confirmDelete(ReportConfig report) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete report " + report.getReportName() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            reportService.delete(report);
            refreshSummary();
            refreshGrid();
            notify("Report deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    private void refreshGrid() {
        List<ReportConfig> reports = reportService.search(searchField.getValue());
        if (!"ALL".equalsIgnoreCase(statusFilter.getValue())) {
            reports = reports.stream()
                .filter(report -> statusFilter.getValue().equalsIgnoreCase(report.getStatus()))
                .toList();
        }
        grid.setItems(reports);
    }

    private void refreshSummary() {
        List<ReportConfig> reports = reportService.findAll();
        totalReportsValue.setText(String.valueOf(reports.size()));
        readyValue.setText(String.valueOf(reportService.countByStatus("READY")));
        failedValue.setText(String.valueOf(reportService.countByStatus("FAILED")));
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}

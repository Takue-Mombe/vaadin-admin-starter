package com.adminpro.ui.views.orders;

import com.adminpro.application.OrderService;
import com.adminpro.domain.OrderRecord;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Route(value = "orders", layout = MainLayout.class)
@PageTitle("Orders | Arc Admin")
@PermitAll
public class OrdersView extends Div {

    private static final List<String> ORDER_STATUSES = List.of("PAID", "PENDING", "TRIAL", "FAILED");
    private static final List<String> PLAN_OPTIONS = List.of("Starter", "Pro", "Pro Annual", "Enterprise", "Trial");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final OrderService orderService;

    private final Grid<OrderRecord> grid = new Grid<>(OrderRecord.class, false);
    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    private final Span totalOrdersValue = new Span("0");
    private final Span revenueValue = new Span("$0");
    private final Span pendingValue = new Span("0");

    public OrdersView(OrderService orderService) {
        this.orderService = orderService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Orders", "Manage billing, status updates, and processing");

        Button exportButton = new Button("Export");
        exportButton.addClassNames("btn", "btn-outline");

        Button addOrderButton = new Button("Add Order", VaadinIcon.PLUS.create());
        addOrderButton.addClassNames("btn", "btn-primary");
        addOrderButton.addClickListener(event -> openDialog(new OrderRecord()));

        header.addAction(exportButton);
        header.addAction(addOrderButton);

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
        stats.add(buildStat("Total Orders", totalOrdersValue, "all tracked records", "neu"));
        stats.add(buildStat("Revenue", revenueValue, "sum of paid and trial invoices", "up"));
        stats.add(buildStat("Pending", pendingValue, "needs processing", "neu"));
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

        searchField.setPlaceholder("Search order, customer, or plan");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("toolbar-search");
        searchField.addValueChangeListener(event -> refreshGrid());

        statusFilter.setLabel("Status");
        statusFilter.setItems(Arrays.asList("ALL", "PAID", "PENDING", "TRIAL", "FAILED"));
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

        grid.addColumn(OrderRecord::getOrderNumber).setHeader("Order").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(OrderRecord::getCustomer).setHeader("Customer").setAutoWidth(true);
        grid.addColumn(OrderRecord::getPlanName).setHeader("Plan").setAutoWidth(true);
        grid.addColumn(order -> toCurrency(order.getAmount())).setHeader("Amount").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(order -> buildStatusBadge(order.getStatus())).setHeader("Status").setAutoWidth(true);
        grid.addColumn(order -> order.getOrderedOn().format(DATE_FMT)).setHeader("Date").setAutoWidth(true);
        grid.addComponentColumn(this::buildRowActions).setHeader("").setAutoWidth(true).setFlexGrow(0);

        Div card = new Div();
        card.addClassName("card");

        Div body = new Div();
        body.addClassName("card-b");
        body.add(grid);

        card.add(body);
        return card;
    }

    private String toCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount == null ? BigDecimal.ZERO : amount);
    }

    private Span buildStatusBadge(String status) {
        Span badge = new Span(status);
        badge.addClassName("status-pill");

        if ("PAID".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--green");
        } else if ("PENDING".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--amber");
        } else if ("FAILED".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--red");
        } else {
            badge.addClassName("status-pill--gray");
        }
        return badge;
    }

    private Div buildRowActions(OrderRecord order) {
        Div actions = new Div();
        actions.addClassName("table-actions");

        Button manage = new Button("Manage");
        manage.addClassNames("btn", "btn-outline", "btn-sm");
        manage.addClickListener(event -> openDialog(order));

        Button delete = new Button("Delete");
        delete.addClassNames("btn", "btn-danger", "btn-sm");
        delete.addClickListener(event -> confirmDelete(order));

        actions.add(manage, delete);
        return actions;
    }

    private void openDialog(OrderRecord order) {
        boolean isNew = order.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add Order" : "Edit Order");
        dialog.setWidth("560px");
        dialog.setCloseOnOutsideClick(false);

        TextField orderNumber = new TextField("Order Number");
        orderNumber.setPlaceholder("NX-9013");
        orderNumber.setWidthFull();

        TextField customer = new TextField("Customer");
        customer.setWidthFull();

        Select<String> plan = new Select<>();
        plan.setLabel("Plan");
        plan.setItems(PLAN_OPTIONS);
        plan.setWidthFull();

        BigDecimalField amount = new BigDecimalField("Amount");
        amount.setWidthFull();

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems(ORDER_STATUSES);
        status.setWidthFull();

        DatePicker date = new DatePicker("Order Date");
        date.setWidthFull();

        Div form = new Div(orderNumber, customer, plan, amount, status, date);
        form.getStyle().set("display", "grid").set("gap", "var(--lumo-space-s)");

        BeanValidationBinder<OrderRecord> binder = new BeanValidationBinder<>(OrderRecord.class);
        binder.forField(orderNumber).asRequired().bind(OrderRecord::getOrderNumber, OrderRecord::setOrderNumber);
        binder.forField(customer).asRequired().bind(OrderRecord::getCustomer, OrderRecord::setCustomer);
        binder.forField(plan).asRequired().bind(OrderRecord::getPlanName, OrderRecord::setPlanName);
        binder.forField(amount).asRequired().bind(OrderRecord::getAmount, OrderRecord::setAmount);
        binder.forField(status).asRequired().bind(OrderRecord::getStatus, OrderRecord::setStatus);
        binder.forField(date).asRequired().bind(OrderRecord::getOrderedOn, OrderRecord::setOrderedOn);
        binder.readBean(order);

        if (plan.getValue() == null) {
            plan.setValue("Starter");
        }
        if (status.getValue() == null) {
            status.setValue("PENDING");
        }
        if (date.getValue() == null) {
            date.setValue(LocalDate.now());
        }

        Button save = new Button(isNew ? "Create" : "Save");
        save.addClassNames("btn", "btn-primary");
        save.addClickListener(event -> {
            try {
                binder.writeBean(order);
                orderService.save(order);
                dialog.close();
                refreshSummary();
                refreshGrid();
                notify(isNew ? "Order created." : "Order updated.", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                notify("Could not save order. Ensure the order number is unique.", NotificationVariant.LUMO_ERROR);
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

    private void confirmDelete(OrderRecord order) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete order " + order.getOrderNumber() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            orderService.delete(order);
            refreshSummary();
            refreshGrid();
            notify("Order deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    private void refreshGrid() {
        List<OrderRecord> orders = orderService.search(searchField.getValue());
        if (!"ALL".equalsIgnoreCase(statusFilter.getValue())) {
            orders = orders.stream()
                .filter(order -> statusFilter.getValue().equalsIgnoreCase(order.getStatus()))
                .toList();
        }
        grid.setItems(orders);
    }

    private void refreshSummary() {
        List<OrderRecord> orders = orderService.findAll();
        totalOrdersValue.setText(String.valueOf(orders.size()));
        revenueValue.setText(toCurrency(orderService.totalRevenue()));
        pendingValue.setText(String.valueOf(orderService.countByStatus("PENDING")));
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}

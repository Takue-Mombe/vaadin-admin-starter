package com.adminpro.ui.views.products;

import com.adminpro.application.ProductService;
import com.adminpro.domain.ProductItem;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products | Arc Admin")
@PermitAll
public class ProductsView extends Div {

    private static final List<String> STATUS_OPTIONS = List.of("ACTIVE", "DRAFT", "ARCHIVED");

    private final ProductService productService;

    private final Grid<ProductItem> grid = new Grid<>(ProductItem.class, false);
    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    private final Span totalProductsValue = new Span("0");
    private final Span activeProductsValue = new Span("0");
    private final Span lowStockValue = new Span("0");

    public ProductsView(ProductService productService) {
        this.productService = productService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Products", "Manage your product catalog and inventory");

        Button addProductButton = new Button("Add Product", VaadinIcon.PLUS.create());
        addProductButton.addClassNames("btn", "btn-primary");
        addProductButton.addClickListener(event -> openDialog(new ProductItem()));

        header.addAction(addProductButton);

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
        stats.add(buildStat("Products", totalProductsValue, "all catalog items", "neu"));
        stats.add(buildStat("Active", activeProductsValue, "currently sellable", "up"));
        stats.add(buildStat("Low Stock", lowStockValue, "below 10 units", "dn"));
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

        searchField.setPlaceholder("Search name, SKU, or category");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("toolbar-search");
        searchField.addValueChangeListener(event -> refreshGrid());

        statusFilter.setLabel("Status");
        statusFilter.setItems(Arrays.asList("ALL", "ACTIVE", "DRAFT", "ARCHIVED"));
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

        grid.addColumn(ProductItem::getName).setHeader("Product").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(ProductItem::getSku).setHeader("SKU").setAutoWidth(true);
        grid.addColumn(ProductItem::getCategory).setHeader("Category").setAutoWidth(true);
        grid.addColumn(item -> NumberFormat.getCurrencyInstance(Locale.US).format(item.getPrice()))
            .setHeader("Price")
            .setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(ProductItem::getStock).setHeader("Stock").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(item -> buildStatusBadge(item.getStatus())).setHeader("Status").setAutoWidth(true);
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

        if ("ACTIVE".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--green");
        } else if ("DRAFT".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--amber");
        } else {
            badge.addClassName("status-pill--gray");
        }
        return badge;
    }

    private Div buildRowActions(ProductItem item) {
        Div actions = new Div();
        actions.addClassName("table-actions");

        Button manage = new Button("Manage");
        manage.addClassNames("btn", "btn-outline", "btn-sm");
        manage.addClickListener(event -> openDialog(item));

        Button delete = new Button("Delete");
        delete.addClassNames("btn", "btn-danger", "btn-sm");
        delete.addClickListener(event -> confirmDelete(item));

        actions.add(manage, delete);
        return actions;
    }

    private void openDialog(ProductItem item) {
        boolean isNew = item.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add Product" : "Edit Product");
        dialog.setWidth("560px");
        dialog.setCloseOnOutsideClick(false);

        TextField name = new TextField("Product Name");
        name.setWidthFull();

        TextField sku = new TextField("SKU");
        sku.setWidthFull();

        TextField category = new TextField("Category");
        category.setWidthFull();

        BigDecimalField price = new BigDecimalField("Price");
        price.setWidthFull();

        IntegerField stock = new IntegerField("Stock");
        stock.setWidthFull();

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems(STATUS_OPTIONS);
        status.setWidthFull();

        Div form = new Div(name, sku, category, price, stock, status);
        form.getStyle().set("display", "grid").set("gap", "var(--lumo-space-s)");

        BeanValidationBinder<ProductItem> binder = new BeanValidationBinder<>(ProductItem.class);
        binder.forField(name).asRequired().bind(ProductItem::getName, ProductItem::setName);
        binder.forField(sku).asRequired().bind(ProductItem::getSku, ProductItem::setSku);
        binder.forField(category).asRequired().bind(ProductItem::getCategory, ProductItem::setCategory);
        binder.forField(price).asRequired().bind(ProductItem::getPrice, ProductItem::setPrice);
        binder.forField(stock).asRequired().bind(ProductItem::getStock, ProductItem::setStock);
        binder.forField(status).asRequired().bind(ProductItem::getStatus, ProductItem::setStatus);
        binder.readBean(item);

        if (status.getValue() == null) {
            status.setValue("ACTIVE");
        }
        if (price.getValue() == null) {
            price.setValue(BigDecimal.ZERO);
        }
        if (stock.getValue() == null) {
            stock.setValue(0);
        }

        Button save = new Button(isNew ? "Create" : "Save");
        save.addClassNames("btn", "btn-primary");
        save.addClickListener(event -> {
            try {
                binder.writeBean(item);
                productService.save(item);
                dialog.close();
                refreshSummary();
                refreshGrid();
                notify(isNew ? "Product created." : "Product updated.", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                notify("Could not save product. Ensure SKU is unique.", NotificationVariant.LUMO_ERROR);
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

    private void confirmDelete(ProductItem item) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete " + item.getName() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            productService.delete(item);
            refreshSummary();
            refreshGrid();
            notify("Product deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    private void refreshGrid() {
        List<ProductItem> products = productService.search(searchField.getValue());
        if (!"ALL".equalsIgnoreCase(statusFilter.getValue())) {
            products = products.stream()
                .filter(item -> statusFilter.getValue().equalsIgnoreCase(item.getStatus()))
                .toList();
        }
        grid.setItems(products);
    }

    private void refreshSummary() {
        List<ProductItem> products = productService.findAll();
        long lowStock = products.stream().filter(item -> item.getStock() != null && item.getStock() < 10).count();

        totalProductsValue.setText(String.valueOf(products.size()));
        activeProductsValue.setText(String.valueOf(productService.countByStatus("ACTIVE")));
        lowStockValue.setText(String.valueOf(lowStock));
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}

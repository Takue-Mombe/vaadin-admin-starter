package com.adminpro.ui.views.users;

import com.adminpro.application.UserService;
import com.adminpro.domain.Role;
import com.adminpro.domain.User;
import com.adminpro.ui.components.ContentCard;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Users page – searchable grid with full CRUD via a dialog.
 *
 * Features:
 *   - Live search (name / email filter)
 *   - Add User button → opens dialog
 *   - Row-level Edit and Delete actions
 *   - BeanValidation-driven form errors
 *   - Success/error toast notifications
 */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | AdminPro")
public class UsersView extends Div {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);
    private final TextField searchField = new TextField();

    public UsersView(UserService userService) {
        this.userService = userService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Users", "Manage team members and their access levels.");
        Button addBtn = new Button("Add User");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openDialog(new User()));
        header.addAction(addBtn);

        add(header);
        add(buildToolbar());
        add(buildGrid());
        refreshGrid(null);
    }

    // ── Toolbar ───────────────────────────────────────────────────────────

    private ContentCard buildToolbar() {
        searchField.setPlaceholder("Search by name or email…");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("320px");
        searchField.addValueChangeListener(e -> refreshGrid(e.getValue()));

        ContentCard card = new ContentCard();
        card.fullWidth();
        card.addContent(searchField);
        return card;
    }

    // ── Grid ──────────────────────────────────────────────────────────────

    private ContentCard buildGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setWidthFull();

        grid.addColumn(User::getFullName)
            .setHeader("Name").setSortable(true).setFlexGrow(2);

        grid.addColumn(User::getEmail)
            .setHeader("Email").setSortable(true).setFlexGrow(3);

        grid.addColumn(u -> u.getRole().name())
            .setHeader("Role").setSortable(true).setFlexGrow(1);

        grid.addComponentColumn(u -> {
            Span badge = new Span(u.isActive() ? "Active" : "Inactive");
            badge.addClassNames("badge", u.isActive() ? "badge--success" : "badge--error");
            return badge;
        }).setHeader("Status").setFlexGrow(1);

        grid.addComponentColumn(u -> buildRowActions(u))
            .setHeader("Actions").setFlexGrow(1).setAutoWidth(true);

        ContentCard card = new ContentCard("All Users");
        card.fullWidth();
        card.addContent(grid);
        return card;
    }

    private HorizontalLayout buildRowActions(User user) {
        Button editBtn = new Button("Edit");
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.addClickListener(e -> openDialog(copyUser(user)));

        Button deleteBtn = new Button("Delete");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> confirmDelete(user));

        HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
        actions.setSpacing(false);
        return actions;
    }

    // ── Dialog ────────────────────────────────────────────────────────────

    private void openDialog(User user) {
        boolean isNew = (user.getId() == null);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Add User" : "Edit User");
        dialog.setWidth("480px");
        dialog.setCloseOnOutsideClick(false);

        // Form fields
        TextField firstName  = new TextField("First Name");
        TextField lastName   = new TextField("Last Name");
        EmailField email     = new EmailField("Email");
        ComboBox<Role> role  = new ComboBox<>("Role");
        role.setItems(Role.values());
        role.setItemLabelGenerator(Role::name);
        Checkbox active = new Checkbox("Active");

        firstName.setWidthFull();
        lastName.setWidthFull();
        email.setWidthFull();
        role.setWidthFull();

        FormLayout form = new FormLayout(firstName, lastName, email, role, active);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("360px", 2)
        );
        form.setColspan(email, 2);

        // Bind with BeanValidation
        BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
        binder.bindInstanceFields(form);
        // active field needs manual binding (name mismatch)
        binder.bind(active, User::isActive, User::setActive);
        binder.readBean(user);

        // Buttons
        Button save = new Button(isNew ? "Create" : "Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                binder.writeBean(user);
                userService.save(user);
                dialog.close();
                refreshGrid(searchField.getValue());
                notify(isNew ? "User created successfully." : "User updated.", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footer = new HorizontalLayout(cancel, save);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();

        dialog.add(form);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    // ── Delete confirm ────────────────────────────────────────────────────

    private void confirmDelete(User user) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete " + user.getFullName() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            userService.delete(user);
            refreshGrid(searchField.getValue());
            notify("User deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void refreshGrid(String filter) {
        grid.setItems(userService.search(filter));
    }

    private User copyUser(User source) {
        // We pass the actual entity; binder writes back to it on save.
        return source;
    }

    private void notify(String message, NotificationVariant variant) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(variant);
    }
}

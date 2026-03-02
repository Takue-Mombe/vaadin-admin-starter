package com.adminpro.ui.views.users;

import com.adminpro.application.UserService;
import com.adminpro.domain.Role;
import com.adminpro.domain.User;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | Arc Admin")
@PermitAll
public class UsersView extends Div {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final UserService userService;

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final TextField searchField = new TextField();
    private final Select<Role> roleFilter = new Select<>();

    private final Span totalUsersValue = new Span("0");
    private final Span activeUsersValue = new Span("0");
    private final Span adminUsersValue = new Span("0");

    public UsersView(UserService userService) {
        this.userService = userService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Users", "Manage team members and their access levels");

        Button filterButton = new Button("Filter");
        filterButton.addClassNames("btn", "btn-outline");

        Button inviteButton = new Button("Invite User");
        inviteButton.addClassNames("btn", "btn-primary");
        inviteButton.addClickListener(event -> openDialog(new User()));

        header.addAction(filterButton);
        header.addAction(inviteButton);

        add(header);
        add(buildSummaryStats());
        add(buildToolbar());
        add(buildGridCard());

        refreshSummary();
        refreshGrid();
    }

    private Div buildSummaryStats() {
        Div stats = new Div();
        stats.addClassNames("stats", "stats--three");

        stats.add(buildSummaryStat("Total Users", totalUsersValue, "live directory", "neu"));
        stats.add(buildSummaryStat("Active", activeUsersValue, "currently enabled", "up"));
        stats.add(buildSummaryStat("Admins", adminUsersValue, "full access", "up"));

        return stats;
    }

    private Div buildSummaryStat(String label, Span value, String delta, String tone) {
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

        searchField.setPlaceholder("Search by name or email");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("toolbar-search");
        searchField.addValueChangeListener(event -> refreshGrid());

        roleFilter.setLabel("Role");
        roleFilter.setItems(Arrays.asList(Role.values()));
        roleFilter.setItemLabelGenerator(Role::name);
        roleFilter.setPlaceholder("All");
        roleFilter.addValueChangeListener(event -> refreshGrid());
        roleFilter.addClassName("toolbar-select");

        body.add(searchField, roleFilter);
        card.add(body);
        return card;
    }

    private Div buildGridCard() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("table-grid");
        grid.setWidthFull();

        grid.addComponentColumn(this::buildUserCell)
            .setHeader("User")
            .setAutoWidth(true)
            .setFlexGrow(1);

        grid.addComponentColumn(user -> {
            Span tag = new Span(user.getRole().name());
            if (user.getRole() == Role.ADMIN) {
                tag.addClassNames("tag", "tag-green");
            } else {
                tag.addClassName("tag");
            }
            return tag;
        }).setHeader("Role").setAutoWidth(true);

        grid.addColumn(this::resolvePlan)
            .setHeader("Plan")
            .setAutoWidth(true);

        grid.addComponentColumn(user -> buildStatusBadge(user.isActive() ? "Active" : "Inactive"))
            .setHeader("Status")
            .setAutoWidth(true);

        grid.addColumn(user -> user.getCreatedAt().format(DATE_FMT))
            .setHeader("Joined")
            .setAutoWidth(true);

        grid.addComponentColumn(this::buildRowActions)
            .setHeader("")
            .setAutoWidth(true)
            .setFlexGrow(0);

        Div card = new Div();
        card.addClassName("card");

        Div body = new Div();
        body.addClassName("card-b");
        body.add(grid);

        card.add(body);
        return card;
    }

    private Div buildUserCell(User user) {
        Div cell = new Div();
        cell.addClassName("u-cell");

        Div avatar = new Div(initials(user));
        avatar.addClassName("u-avi");

        Div text = new Div();
        Span name = new Span(user.getFullName());
        name.addClassName("u-name");
        Span email = new Span(user.getEmail());
        email.addClassName("u-email");
        text.add(name, email);

        cell.add(avatar, text);
        return cell;
    }

    private Span buildStatusBadge(String status) {
        Span badge = new Span(status);
        badge.addClassName("status-pill");
        badge.addClassName("Active".equalsIgnoreCase(status) ? "status-pill--green" : "status-pill--red");
        return badge;
    }

    private Div buildRowActions(User user) {
        Div actions = new Div();
        actions.addClassName("table-actions");

        Button manage = new Button("Manage");
        manage.addClassNames("btn", "btn-outline", "btn-sm");
        manage.addClickListener(event -> openDialog(user));

        Button delete = new Button("Delete");
        delete.addClassNames("btn", "btn-danger", "btn-sm");
        delete.addClickListener(event -> confirmDelete(user));

        actions.add(manage, delete);
        return actions;
    }

    private void openDialog(User user) {
        boolean isNew = user.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Invite User" : "Manage User");
        dialog.setWidth("520px");
        dialog.setCloseOnOutsideClick(false);

        TextField firstName = new TextField("First Name");
        TextField lastName = new TextField("Last Name");
        EmailField email = new EmailField("Email");
        Select<Role> role = new Select<>();
        role.setLabel("Role");
        role.setItems(Arrays.asList(Role.values()));
        role.setItemLabelGenerator(Role::name);
        Checkbox active = new Checkbox("Active");

        firstName.setWidthFull();
        lastName.setWidthFull();
        email.setWidthFull();
        role.setWidthFull();

        FormLayout form = new FormLayout(firstName, lastName, email, role, active);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("440px", 2)
        );
        form.setColspan(email, 2);
        form.setColspan(role, 2);
        form.setColspan(active, 2);

        BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
        binder.forField(firstName).asRequired().bind(User::getFirstName, User::setFirstName);
        binder.forField(lastName).asRequired().bind(User::getLastName, User::setLastName);
        binder.forField(email).asRequired().bind(User::getEmail, User::setEmail);
        binder.forField(role).asRequired().bind(User::getRole, User::setRole);
        binder.bind(active, User::isActive, User::setActive);
        binder.readBean(user);

        Button save = new Button(isNew ? "Create" : "Save");
        save.addClassNames("btn", "btn-primary");
        save.addClickListener(event -> {
            try {
                binder.writeBean(user);
                userService.save(user);
                dialog.close();
                refreshSummary();
                refreshGrid();
                notify(
                    isNew ? "User invited. Temporary password: " + UserService.DEFAULT_PASSWORD : "User updated.",
                    NotificationVariant.LUMO_SUCCESS
                );
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                notify("Could not save user. Check if the email already exists.", NotificationVariant.LUMO_ERROR);
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

    private void confirmDelete(User user) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete " + user.getFullName() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            userService.delete(user);
            refreshSummary();
            refreshGrid();
            notify("User deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    private void refreshGrid() {
        List<User> users = userService.search(searchField.getValue());
        if (roleFilter.getValue() != null) {
            users = users.stream().filter(user -> user.getRole() == roleFilter.getValue()).toList();
        }
        grid.setItems(users);
    }

    private void refreshSummary() {
        List<User> users = userService.findAll();
        totalUsersValue.setText(String.valueOf(users.size()));
        activeUsersValue.setText(String.valueOf(users.stream().filter(User::isActive).count()));
        adminUsersValue.setText(String.valueOf(users.stream().filter(user -> user.getRole() == Role.ADMIN).count()));
    }

    private String resolvePlan(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Enterprise";
            case MANAGER -> "Pro";
            case VIEWER -> "Starter";
        };
    }

    private String initials(User user) {
        String first = user.getFirstName() == null || user.getFirstName().isBlank()
            ? "U"
            : user.getFirstName().substring(0, 1).toUpperCase();
        String last = user.getLastName() == null || user.getLastName().isBlank()
            ? "X"
            : user.getLastName().substring(0, 1).toUpperCase();
        return first + last;
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}

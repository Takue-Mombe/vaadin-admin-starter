package com.adminpro.ui.views.settings;

import com.adminpro.application.SettingsService;
import com.adminpro.application.UserService;
import com.adminpro.domain.User;
import com.adminpro.domain.UserSettings;
import com.adminpro.security.CurrentUserService;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | Arc Admin")
@PermitAll
public class SettingsView extends Div {

    private static final List<String> TIMEZONES = List.of(
        "UTC",
        "America/New_York",
        "America/Chicago",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Asia/Kolkata"
    );

    public SettingsView(SettingsService settingsService,
                        UserService userService,
                        CurrentUserService currentUserService) {
        addClassName("page-content");
        setWidthFull();

        User currentUser = currentUserService.getCurrentUser()
            .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));

        UserSettings settings = settingsService.load();
        settings.setDisplayName(currentUser.getFullName());
        settings.setEmail(currentUser.getEmail());

        BeanValidationBinder<User> profileBinder = new BeanValidationBinder<>(User.class);
        BeanValidationBinder<UserSettings> settingsBinder = new BeanValidationBinder<>(UserSettings.class);

        TextField firstName = inputField(new TextField(), "First name");
        TextField lastName = inputField(new TextField(), "Last name");
        EmailField email = inputField(new EmailField(), "Email");
        TextField phone = inputField(new TextField(), "+1 212 555 0142");

        ComboBox<String> timezone = new ComboBox<>();
        timezone.setItems(TIMEZONES);
        timezone.addClassName("text-input");
        timezone.setWidth("240px");

        Checkbox emailNotifications = toggle(true);
        Checkbox darkModeDefault = toggle(false);

        PasswordField newPassword = new PasswordField();
        newPassword.setPlaceholder("New password (min 8 chars)");
        newPassword.addClassName("text-input");
        newPassword.setWidth("240px");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPlaceholder("Confirm new password");
        confirmPassword.addClassName("text-input");
        confirmPassword.setWidth("240px");

        profileBinder.forField(firstName).asRequired().bind(User::getFirstName, User::setFirstName);
        profileBinder.forField(lastName).asRequired().bind(User::getLastName, User::setLastName);
        profileBinder.forField(email).asRequired().bind(User::getEmail, User::setEmail);

        settingsBinder.forField(phone).bind(UserSettings::getPhone, UserSettings::setPhone);
        settingsBinder.forField(timezone).asRequired().bind(UserSettings::getTimezone, UserSettings::setTimezone);
        settingsBinder.forField(emailNotifications).bind(UserSettings::isEmailNotifications, UserSettings::setEmailNotifications);
        settingsBinder.forField(darkModeDefault).bind(UserSettings::isDarkMode, UserSettings::setDarkMode);

        PageHeader header = new PageHeader("Settings", "Manage your profile and authentication preferences");

        Button saveButton = new Button("Save Changes");
        saveButton.addClassNames("btn", "btn-primary");
        saveButton.addClickListener(event -> {
            String originalEmail = currentUser.getEmail();
            try {
                profileBinder.writeBean(currentUser);
                settingsBinder.writeBean(settings);

                boolean passwordChanged = false;
                String newPass = newPassword.getValue() == null ? "" : newPassword.getValue().trim();
                String confirmPass = confirmPassword.getValue() == null ? "" : confirmPassword.getValue().trim();

                if (!newPass.isBlank() || !confirmPass.isBlank()) {
                    if (newPass.length() < 8) {
                        notify("Password must be at least 8 characters.", NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        notify("Password confirmation does not match.", NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    userService.updatePassword(currentUser, newPass);
                    passwordChanged = true;
                } else {
                    userService.save(currentUser);
                }

                settings.setDisplayName(currentUser.getFullName());
                settings.setEmail(currentUser.getEmail());
                settingsService.save(settings);

                getElement().executeJs(
                    "document.documentElement.setAttribute('data-theme', $0)",
                    settings.isDarkMode() ? "dark" : "light"
                );

                newPassword.clear();
                confirmPassword.clear();

                boolean emailChanged = !originalEmail.equalsIgnoreCase(currentUser.getEmail());
                if (emailChanged || passwordChanged) {
                    notify("Profile saved. Use updated credentials for the next request.", NotificationVariant.LUMO_SUCCESS);
                } else {
                    notify("Profile saved successfully.", NotificationVariant.LUMO_SUCCESS);
                }
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                notify("Could not save profile. Email may already exist.", NotificationVariant.LUMO_ERROR);
            }
        });
        header.addAction(saveButton);

        add(header);
        add(buildProfileSection(firstName, lastName, email, timezone, phone));
        add(buildPreferencesSection(emailNotifications, darkModeDefault));
        add(buildSecuritySection(newPassword, confirmPassword));

        profileBinder.readBean(currentUser);
        settingsBinder.readBean(settings);
        getElement().executeJs(
            "document.documentElement.setAttribute('data-theme', $0)",
            settings.isDarkMode() ? "dark" : "light"
        );
    }

    private Div buildProfileSection(TextField firstName,
                                    TextField lastName,
                                    EmailField email,
                                    ComboBox<String> timezone,
                                    TextField phone) {
        Div section = buildSection("Profile");
        section.add(buildRow("First Name", "Visible in user profile and activity feeds.", firstName));
        section.add(buildRow("Last Name", "Visible in user profile and activity feeds.", lastName));
        section.add(buildRow("Email Address", "Used for basic auth login.", email));
        section.add(buildRow("Timezone", "All dates shown in this zone.", timezone));
        section.add(buildRow("Phone", "Optional contact for account recovery.", phone));
        return section;
    }

    private Div buildPreferencesSection(Checkbox emailNotifications,
                                        Checkbox darkModeDefault) {
        Div section = buildSection("Preferences");
        section.add(buildRow("Email Notifications", "Receive email digests of activity.", emailNotifications));
        section.add(buildRow("Use Dark Theme", "Default theme for future sessions.", darkModeDefault));
        return section;
    }

    private Div buildSecuritySection(PasswordField newPassword,
                                     PasswordField confirmPassword) {
        Div section = buildSection("Security");
        section.add(buildRow("New Password", "Leave blank to keep existing password.", newPassword));
        section.add(buildRow("Confirm Password", "Must match the new password value.", confirmPassword));
        return section;
    }

    private Div buildSection(String title) {
        Div section = new Div();
        section.addClassName("set-section");

        Div head = new Div(new Span(title));
        head.addClassName("set-head");

        section.add(head);
        return section;
    }

    private Div buildRow(String label, String description, Component control) {
        Div row = new Div();
        row.addClassName("set-row");

        Div text = new Div();

        Span labelSpan = new Span(label);
        labelSpan.addClassName("set-label");

        Span descriptionSpan = new Span(description);
        descriptionSpan.addClassName("set-desc");

        text.add(labelSpan, descriptionSpan);

        row.add(text, control);
        return row;
    }

    private Checkbox toggle(boolean value) {
        Checkbox checkbox = new Checkbox();
        checkbox.addClassName("set-toggle");
        checkbox.setValue(value);
        return checkbox;
    }

    private TextField inputField(TextField field, String placeholder) {
        field.addClassName("text-input");
        field.setPlaceholder(placeholder);
        field.setWidth("240px");
        return field;
    }

    private EmailField inputField(EmailField field, String placeholder) {
        field.addClassName("text-input");
        field.setPlaceholder(placeholder);
        field.setWidth("240px");
        return field;
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3500, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}

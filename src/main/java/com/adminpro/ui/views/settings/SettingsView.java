package com.adminpro.ui.views.settings;

import com.adminpro.application.SettingsService;
import com.adminpro.domain.UserSettings;
import com.adminpro.ui.components.ContentCard;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Settings page – profile form + preference toggles.
 *
 * Sections:
 *   1. Profile  (display name, email, phone)
 *   2. Preferences  (timezone, email notifications, dark mode placeholder)
 */
@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | AdminPro")
public class SettingsView extends Div {

    private static final List<String> TIMEZONES = List.of(
        "UTC", "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "Europe/London", "Europe/Paris",
        "Europe/Berlin", "Asia/Tokyo", "Asia/Shanghai", "Asia/Kolkata",
        "Australia/Sydney"
    );

    public SettingsView(SettingsService settingsService) {
        addClassName("page-content");
        setWidthFull();

        UserSettings settings = settingsService.load();
        BeanValidationBinder<UserSettings> binder = new BeanValidationBinder<>(UserSettings.class);

        add(new PageHeader("Settings", "Manage your profile and application preferences."));
        add(buildProfileSection(binder));
        add(new Hr());
        add(buildPreferencesSection(binder));
        add(buildSaveButton(settings, binder, settingsService));

        binder.readBean(settings);
    }

    // ── Profile ───────────────────────────────────────────────────────────

    private ContentCard buildProfileSection(BeanValidationBinder<UserSettings> binder) {
        TextField displayName = new TextField("Display Name");
        displayName.setWidthFull();

        EmailField email = new EmailField("Email Address");
        email.setWidthFull();

        TextField phone = new TextField("Phone (optional)");
        phone.setWidthFull();

        binder.forField(displayName).bind(UserSettings::getDisplayName, UserSettings::setDisplayName);
        binder.forField(email).bind(UserSettings::getEmail, UserSettings::setEmail);
        binder.forField(phone).bind(UserSettings::getPhone, UserSettings::setPhone);

        FormLayout form = new FormLayout(displayName, email, phone);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("480px", 2)
        );
        form.setColspan(displayName, 2);

        ContentCard card = new ContentCard("Profile", "Your public-facing name and contact information.");
        card.fullWidth();
        card.addContent(form);
        return card;
    }

    // ── Preferences ───────────────────────────────────────────────────────

    private ContentCard buildPreferencesSection(BeanValidationBinder<UserSettings> binder) {
        ComboBox<String> timezone = new ComboBox<>("Timezone");
        timezone.setItems(TIMEZONES);
        timezone.setWidth("260px");

        Checkbox emailNotifications = new Checkbox("Receive email notifications");
        Checkbox darkMode = new Checkbox("Dark mode (Phase 2 – coming soon)");
        darkMode.setEnabled(false);   // placeholder for Phase 2

        binder.forField(timezone).bind(UserSettings::getTimezone, UserSettings::setTimezone);
        binder.forField(emailNotifications)
              .bind(UserSettings::isEmailNotifications, UserSettings::setEmailNotifications);
        binder.forField(darkMode)
              .bind(UserSettings::isDarkMode, UserSettings::setDarkMode);

        FormLayout form = new FormLayout(timezone, emailNotifications, darkMode);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        ContentCard card = new ContentCard("Preferences", "Application behaviour and display options.");
        card.fullWidth();
        card.addContent(form);
        return card;
    }

    // ── Save button ───────────────────────────────────────────────────────

    private Div buildSaveButton(UserSettings settings,
                                BeanValidationBinder<UserSettings> binder,
                                SettingsService service) {
        Button save = new Button("Save Changes");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                binder.writeBean(settings);
                service.save(settings);
                notify("Settings saved successfully.", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            }
        });

        Div wrapper = new Div(save);
        wrapper.getStyle()
               .set("display", "flex")
               .set("justify-content", "flex-end")
               .set("margin-top", "var(--lumo-space-l)");
        return wrapper;
    }

    private void notify(String message, NotificationVariant variant) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(variant);
    }
}

package com.adminpro.ui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.theme.Theme;

/**
 * Vaadin 24 application shell configurator.
 *
 * This is the single place allowed to declare @Theme.
 * It also sets the viewport meta tag and any global HTML head content.
 */
@Theme("adminpro")
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes")
@Meta(name = "description", content = "Vaadin Admin Pro Kit — professional admin UI starter")
public class AppShell implements AppShellConfigurator {
}

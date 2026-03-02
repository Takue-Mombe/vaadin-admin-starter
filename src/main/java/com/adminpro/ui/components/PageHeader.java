package com.adminpro.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;

/**
 * Page-level header block: title, optional description, and an
 * optional right-side actions slot (buttons etc.).
 *
 * Usage:
 * <pre>
 *   PageHeader header = new PageHeader("Users", "Manage your team");
 *   header.addAction(new Button("Add User", ...));
 *   add(header);
 * </pre>
 */
public class PageHeader extends Div {

    private final Div actionsSlot;

    public PageHeader(String title) {
        this(title, null);
    }

    public PageHeader(String title, String description) {
        addClassName("page-header");

        Div textBlock = new Div();
        textBlock.addClassName("page-header__text");

        H1 titleEl = new H1(title);
        titleEl.addClassName("page-header__title");
        textBlock.add(titleEl);

        if (description != null && !description.isBlank()) {
            Paragraph desc = new Paragraph(description);
            desc.addClassName("page-header__description");
            textBlock.add(desc);
        }

        actionsSlot = new Div();
        actionsSlot.addClassName("page-header__actions");

        add(textBlock, actionsSlot);
    }

    /** Append a component (e.g. Button) to the right-hand actions area. */
    public void addAction(Component component) {
        actionsSlot.add(component);
    }
}

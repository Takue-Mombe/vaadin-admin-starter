package com.adminpro.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;

/**
 * A styled container card used to group related content on a page.
 *
 * Usage:
 * <pre>
 *   ContentCard card = new ContentCard("Recent Activity");
 *   card.addContent(myGrid);
 *   add(card);
 * </pre>
 */
public class ContentCard extends Div {

    private final Div body;

    public ContentCard() {
        this(null, null);
    }

    public ContentCard(String title) {
        this(title, null);
    }

    public ContentCard(String title, String subtitle) {
        addClassName("content-card");

        if (title != null && !title.isBlank()) {
            H2 titleEl = new H2(title);
            titleEl.addClassName("form-section__title");
            add(titleEl);

            if (subtitle != null && !subtitle.isBlank()) {
                Paragraph sub = new Paragraph(subtitle);
                sub.addClassName("form-section__subtitle");
                add(sub);
            }
        }

        body = new Div();
        body.getStyle().set("margin-top", "var(--lumo-space-m)");
        add(body);
    }

    /** Add child components into the card body. */
    public void addContent(Component... components) {
        body.add(components);
    }

    /** Convenience: set width to 100% */
    public ContentCard fullWidth() {
        setWidthFull();
        body.setWidthFull();
        return this;
    }
}

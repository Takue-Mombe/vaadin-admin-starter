package com.adminpro.ui.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.adminpro.domain.DashboardStat;

/**
 * A metric tile showing a label, large value, trend text, and icon.
 *
 * Usage:
 * <pre>
 *   add(new StatCard(stat));
 * </pre>
 */
public class StatCard extends Div {

    public StatCard(DashboardStat stat) {
        addClassName("stat-card");

        // ── Header row (label + icon) ──────────────────────────
        Div header = new Div();
        header.addClassName("stat-card__header");

        Span label = new Span(stat.getLabel());
        label.addClassName("stat-card__label");

        Div iconBox = new Div();
        iconBox.addClassName("stat-card__icon");
        Icon icon = resolveIcon(stat.getIconName());
        iconBox.add(icon);

        header.add(label, iconBox);

        // ── Value ──────────────────────────────────────────────
        Span value = new Span(stat.getValue());
        value.addClassName("stat-card__value");

        // ── Trend ──────────────────────────────────────────────
        Span trend = new Span(stat.getTrend());
        trend.addClassName("stat-card__trend");

        add(header, value, trend);
    }

    private Icon resolveIcon(String iconName) {
        try {
            // Vaadin icon names are like "vaadin:users" -> VaadinIcon.USERS
            String name = iconName.replace("vaadin:", "").toUpperCase().replace("-", "_");
            return VaadinIcon.valueOf(name).create();
        } catch (IllegalArgumentException e) {
            return VaadinIcon.CIRCLE.create();
        }
    }
}

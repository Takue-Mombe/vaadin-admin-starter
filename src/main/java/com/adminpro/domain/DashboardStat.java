package com.adminpro.domain;

/**
 * A single stat tile shown on the Dashboard.
 * Not persisted – DashboardService builds these from live DB queries.
 */
public class DashboardStat {

    private final String label;
    private final String value;
    private final String trend;     // e.g. "+12%" or "stable"
    private final String iconName;  // Vaadin icon name, e.g. "vaadin:users"

    public DashboardStat(String label, String value, String trend, String iconName) {
        this.label    = label;
        this.value    = value;
        this.trend    = trend;
        this.iconName = iconName;
    }

    public String getLabel()    { return label; }
    public String getValue()    { return value; }
    public String getTrend()    { return trend; }
    public String getIconName() { return iconName; }
}

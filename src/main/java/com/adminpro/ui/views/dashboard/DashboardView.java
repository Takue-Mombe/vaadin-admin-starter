package com.adminpro.ui.views.dashboard;

import com.adminpro.application.DashboardService;
import com.adminpro.domain.DashboardStat;
import com.adminpro.domain.User;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Arc Admin")
@PermitAll
public class DashboardView extends Div {

    private static final DateTimeFormatter PAGE_DATE_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    private static final DateTimeFormatter FEED_DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static final List<BarDatum> WEEKLY_DATA = List.of(
        new BarDatum("Mon", 58, 9),
        new BarDatum("Tue", 72, 11),
        new BarDatum("Wed", 54, 8),
        new BarDatum("Thu", 88, 13),
        new BarDatum("Fri", 96, 15),
        new BarDatum("Sat", 43, 6),
        new BarDatum("Sun", 28, 4)
    );

    private final Div trendChartHost = new Div();
    private final Span trendPrimary = new Span();
    private final Span trendSecondary = new Span();
    private final List<Button> rangeButtons = new ArrayList<>();
    private final List<Button> metricButtons = new ArrayList<>();

    private final Div weeklyBarsHost = new Div();
    private final Span weeklyDay = new Span();
    private final Span weeklyPrimary = new Span();
    private final Span weeklySecondary = new Span();
    private final List<Button> weeklyModeButtons = new ArrayList<>();
    private final List<Div> weeklyBarColumns = new ArrayList<>();

    private final Div conversionChartHost = new Div();
    private final Span conversionPrimary = new Span();
    private final Span conversionSecondary = new Span();
    private final List<Button> conversionButtons = new ArrayList<>();

    private String activeRange = "1Y";
    private String activeMetric = "Revenue";
    private boolean weeklyRevenueMode;
    private String activeChannel = "All";

    public DashboardView(DashboardService dashboardService) {
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Overview", LocalDate.now().format(PAGE_DATE_FMT));

        Button export = new Button("Export");
        export.addClassNames("btn", "btn-outline");

        Button newReport = new Button("New Report", VaadinIcon.PLUS.create());
        newReport.addClassNames("btn", "btn-primary");
        newReport.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("reports")));

        header.addAction(export);
        header.addAction(newReport);

        add(header);
        add(buildStatsRow(dashboardService.getStats()));
        add(buildTopChartsRow());
        add(buildPerformanceRow());
        add(buildBottomRow(dashboardService.getRecentActivity()));
    }

    private Div buildStatsRow(List<DashboardStat> statsFromService) {
        String activeUsers = statsFromService.stream()
            .filter(stat -> "Active Users".equalsIgnoreCase(stat.getLabel()))
            .map(DashboardStat::getValue)
            .findFirst()
            .orElse("0");

        Div row = new Div();
        row.addClassName("stats");

        row.add(buildStat("Revenue", "$84,290", "+12.4% from last month", "up"));
        row.add(buildStat("Active Users", activeUsers, "+8.1% from last month", "up"));
        row.add(buildStat("Conversion", "6.84%", "-0.3% from last month", "dn"));
        row.add(buildStat("Open Tickets", "127", "-5.6% resolved more", "up"));

        return row;
    }

    private Div buildTopChartsRow() {
        Div row = new Div();
        row.addClassNames("row", "c64");

        CardShell trendCard = buildCard("Performance Trend", "interactive explorer", null);
        trendCard.body().add(buildTrendPanel());

        CardShell trafficCard = buildCard("Traffic Sources", null, null);
        trafficCard.body().add(buildTrafficSources());

        row.add(trendCard.card(), trafficCard.card());
        return row;
    }

    private Div buildPerformanceRow() {
        Div row = new Div();
        row.addClassNames("row", "c2");

        CardShell weeklyCard = buildCard("Weekly Activity", "click bars to inspect", null);
        weeklyCard.body().add(buildWeeklyPanel());

        CardShell conversionCard = buildCard("Conversion Trend", "channel comparison", null);
        conversionCard.body().add(buildConversionPanel());

        row.add(weeklyCard.card(), conversionCard.card());
        return row;
    }

    private Div buildBottomRow(List<User> recentUsers) {
        Div row = new Div();
        row.addClassNames("row", "c64");

        Button viewAll = new Button("View all");
        viewAll.addClassNames("btn", "btn-outline", "btn-sm");
        viewAll.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("orders")));

        CardShell ordersCard = buildCard("Recent Orders", null, viewAll);
        ordersCard.body().add(buildRecentOrdersGrid());

        CardShell activityCard = buildCard("Activity", null, null);
        activityCard.body().add(buildActivityFeed(recentUsers));

        row.add(ordersCard.card(), activityCard.card());
        return row;
    }

    private Div buildTrendPanel() {
        Div shell = new Div();
        shell.addClassName("interactive-shell");

        shell.add(buildChipGroup(
            List.of("7D", "30D", "90D", "1Y"),
            activeRange,
            rangeButtons,
            range -> {
                activeRange = range;
                updateTrendChart();
            }
        ));

        shell.add(buildChipGroup(
            List.of("Revenue", "Users", "Conversion"),
            activeMetric,
            metricButtons,
            metric -> {
                activeMetric = metric;
                updateTrendChart();
            }
        ));

        Div summary = new Div();
        summary.addClassName("trend-summary");

        trendPrimary.addClassName("trend-summary__value");
        trendSecondary.addClassName("trend-summary__hint");
        summary.add(trendPrimary, trendSecondary);

        trendChartHost.addClassName("chart-host");
        shell.add(summary, trendChartHost);

        updateTrendChart();
        return shell;
    }

    private void updateTrendChart() {
        List<ChartPoint> points = resolveTrendPoints(activeMetric, activeRange);
        trendChartHost.getElement().setProperty("innerHTML", renderAreaChart(points));

        int latest = points.get(points.size() - 1).value();
        int previous = points.get(points.size() - 2).value();
        int delta = latest - previous;

        if ("Revenue".equals(activeMetric)) {
            trendPrimary.setText("$" + (70 + latest) + "K");
            trendSecondary.setText((delta >= 0 ? "+" : "") + delta + " pts vs previous period");
        } else if ("Users".equals(activeMetric)) {
            trendPrimary.setText(String.valueOf(2400 + latest * 18));
            trendSecondary.setText((delta >= 0 ? "+" : "") + (delta * 11) + " users vs previous period");
        } else {
            trendPrimary.setText(String.format(Locale.US, "%.2f%%", latest / 10.0));
            trendSecondary.setText((delta >= 0 ? "+" : "") + String.format(Locale.US, "%.2f", delta / 10.0) + " pts");
        }
    }

    private Div buildWeeklyPanel() {
        Div shell = new Div();
        shell.addClassName("interactive-shell");

        shell.add(buildChipGroup(
            List.of("Orders", "Revenue"),
            "Orders",
            weeklyModeButtons,
            mode -> {
                weeklyRevenueMode = "Revenue".equals(mode);
                renderWeeklyBars();
            }
        ));

        weeklyBarsHost.addClassNames("bars", "bars--interactive");

        Div detail = new Div();
        detail.addClassName("bar-detail");

        weeklyDay.addClassName("bar-detail__day");
        weeklyPrimary.addClassName("bar-detail__primary");
        weeklySecondary.addClassName("bar-detail__secondary");
        detail.add(weeklyDay, weeklyPrimary, weeklySecondary);

        shell.add(weeklyBarsHost, detail);
        renderWeeklyBars();
        return shell;
    }

    private void renderWeeklyBars() {
        weeklyBarsHost.removeAll();
        weeklyBarColumns.clear();

        int max = WEEKLY_DATA.stream()
            .mapToInt(item -> weeklyRevenueMode ? item.revenue() : item.orders())
            .max()
            .orElse(1);

        int peakIndex = 0;
        int peakValue = Integer.MIN_VALUE;

        for (int i = 0; i < WEEKLY_DATA.size(); i++) {
            BarDatum datum = WEEKLY_DATA.get(i);
            int value = weeklyRevenueMode ? datum.revenue() : datum.orders();
            if (value > peakValue) {
                peakValue = value;
                peakIndex = i;
            }

            int height = Math.max(8, (int) Math.round((value * 100.0) / max));

            Div col = new Div();
            col.addClassNames("bar-col", "bar-col--interactive");

            Div fill = new Div();
            fill.addClassName("bar-fill");
            fill.getStyle().set("height", height + "%");

            Span axis = new Span(datum.day());
            axis.addClassName("bar-x");

            int index = i;
            col.addClickListener(event -> selectWeeklyBar(index));
            col.add(fill, axis);

            weeklyBarColumns.add(col);
            weeklyBarsHost.add(col);
        }

        selectWeeklyBar(peakIndex);
    }

    private void selectWeeklyBar(int index) {
        for (int i = 0; i < weeklyBarColumns.size(); i++) {
            weeklyBarColumns.get(i).getElement().getClassList().set("active", i == index);
        }

        BarDatum selected = WEEKLY_DATA.get(index);
        weeklyDay.setText(selected.day());

        if (weeklyRevenueMode) {
            weeklyPrimary.setText("$" + selected.revenue() + "K revenue");
            weeklySecondary.setText(selected.orders() + " orders processed");
        } else {
            weeklyPrimary.setText(selected.orders() + " orders");
            weeklySecondary.setText("$" + selected.revenue() + "K revenue");
        }
    }

    private Div buildConversionPanel() {
        Div shell = new Div();
        shell.addClassName("interactive-shell");

        shell.add(buildChipGroup(
            List.of("All", "Web", "Mobile", "Enterprise"),
            activeChannel,
            conversionButtons,
            channel -> {
                activeChannel = channel;
                updateConversionChart();
            }
        ));

        Div summary = new Div();
        summary.addClassName("trend-summary");

        conversionPrimary.addClassName("trend-summary__value");
        conversionSecondary.addClassName("trend-summary__hint");
        summary.add(conversionPrimary, conversionSecondary);

        conversionChartHost.addClassName("chart-host");
        shell.add(summary, conversionChartHost);

        updateConversionChart();
        return shell;
    }

    private void updateConversionChart() {
        List<ChartPoint> points = resolveChannelPoints(activeChannel);
        conversionChartHost.getElement().setProperty("innerHTML", renderAreaChart(points));

        int latest = points.get(points.size() - 1).value();
        int previous = points.get(points.size() - 2).value();
        double latestRate = latest / 10.0;
        double delta = (latest - previous) / 10.0;

        conversionPrimary.setText(String.format(Locale.US, "%.2f%%", latestRate));
        conversionSecondary.setText(
            activeChannel + " channel " + (delta >= 0 ? "+" : "") + String.format(Locale.US, "%.2f", delta) + " pts"
        );
    }

    private Div buildChipGroup(List<String> labels,
                               String selected,
                               List<Button> bucket,
                               Consumer<String> onSelect) {
        Div row = new Div();
        row.addClassName("chip-row");

        for (String label : labels) {
            Button chip = new Button(label);
            chip.addClassName("chip-btn");
            if (label.equals(selected)) {
                chip.addClassName("on");
            }

            chip.addClickListener(event -> {
                setActiveChip(bucket, chip);
                onSelect.accept(label);
            });

            bucket.add(chip);
            row.add(chip);
        }

        return row;
    }

    private void setActiveChip(List<Button> chips, Button active) {
        for (Button chip : chips) {
            chip.removeClassName("on");
        }
        active.addClassName("on");
    }

    private List<ChartPoint> resolveTrendPoints(String metric, String range) {
        return switch (metric) {
            case "Users" -> switch (range) {
                case "7D" -> List.of(
                    new ChartPoint("Mon", 42), new ChartPoint("Tue", 51), new ChartPoint("Wed", 46),
                    new ChartPoint("Thu", 58), new ChartPoint("Fri", 61), new ChartPoint("Sat", 48),
                    new ChartPoint("Sun", 44)
                );
                case "30D" -> List.of(
                    new ChartPoint("W1", 45), new ChartPoint("W2", 52), new ChartPoint("W3", 57), new ChartPoint("W4", 63)
                );
                case "90D" -> List.of(
                    new ChartPoint("Jan", 38), new ChartPoint("Feb", 49), new ChartPoint("Mar", 62)
                );
                default -> List.of(
                    new ChartPoint("Jan", 28), new ChartPoint("Feb", 31), new ChartPoint("Mar", 36),
                    new ChartPoint("Apr", 40), new ChartPoint("May", 44), new ChartPoint("Jun", 49),
                    new ChartPoint("Jul", 53), new ChartPoint("Aug", 56), new ChartPoint("Sep", 60),
                    new ChartPoint("Oct", 63), new ChartPoint("Nov", 66), new ChartPoint("Dec", 70)
                );
            };
            case "Conversion" -> switch (range) {
                case "7D" -> List.of(
                    new ChartPoint("Mon", 58), new ChartPoint("Tue", 60), new ChartPoint("Wed", 57),
                    new ChartPoint("Thu", 64), new ChartPoint("Fri", 66), new ChartPoint("Sat", 62),
                    new ChartPoint("Sun", 61)
                );
                case "30D" -> List.of(
                    new ChartPoint("W1", 55), new ChartPoint("W2", 59), new ChartPoint("W3", 63), new ChartPoint("W4", 68)
                );
                case "90D" -> List.of(
                    new ChartPoint("Jan", 54), new ChartPoint("Feb", 61), new ChartPoint("Mar", 68)
                );
                default -> List.of(
                    new ChartPoint("Jan", 46), new ChartPoint("Feb", 49), new ChartPoint("Mar", 52),
                    new ChartPoint("Apr", 55), new ChartPoint("May", 57), new ChartPoint("Jun", 60),
                    new ChartPoint("Jul", 62), new ChartPoint("Aug", 64), new ChartPoint("Sep", 66),
                    new ChartPoint("Oct", 67), new ChartPoint("Nov", 68), new ChartPoint("Dec", 69)
                );
            };
            default -> switch (range) {
                case "7D" -> List.of(
                    new ChartPoint("Mon", 52), new ChartPoint("Tue", 60), new ChartPoint("Wed", 57),
                    new ChartPoint("Thu", 69), new ChartPoint("Fri", 74), new ChartPoint("Sat", 66),
                    new ChartPoint("Sun", 61)
                );
                case "30D" -> List.of(
                    new ChartPoint("W1", 48), new ChartPoint("W2", 56), new ChartPoint("W3", 64), new ChartPoint("W4", 73)
                );
                case "90D" -> List.of(
                    new ChartPoint("Jan", 44), new ChartPoint("Feb", 58), new ChartPoint("Mar", 72)
                );
                default -> List.of(
                    new ChartPoint("Jan", 28), new ChartPoint("Feb", 33), new ChartPoint("Mar", 37),
                    new ChartPoint("Apr", 41), new ChartPoint("May", 46), new ChartPoint("Jun", 52),
                    new ChartPoint("Jul", 57), new ChartPoint("Aug", 61), new ChartPoint("Sep", 66),
                    new ChartPoint("Oct", 70), new ChartPoint("Nov", 73), new ChartPoint("Dec", 78)
                );
            };
        };
    }

    private List<ChartPoint> resolveChannelPoints(String channel) {
        return switch (channel) {
            case "Web" -> List.of(
                new ChartPoint("W1", 52), new ChartPoint("W2", 55), new ChartPoint("W3", 58), new ChartPoint("W4", 61),
                new ChartPoint("W5", 63), new ChartPoint("W6", 64), new ChartPoint("W7", 67), new ChartPoint("W8", 70)
            );
            case "Mobile" -> List.of(
                new ChartPoint("W1", 47), new ChartPoint("W2", 50), new ChartPoint("W3", 53), new ChartPoint("W4", 57),
                new ChartPoint("W5", 59), new ChartPoint("W6", 61), new ChartPoint("W7", 62), new ChartPoint("W8", 64)
            );
            case "Enterprise" -> List.of(
                new ChartPoint("W1", 40), new ChartPoint("W2", 45), new ChartPoint("W3", 48), new ChartPoint("W4", 52),
                new ChartPoint("W5", 56), new ChartPoint("W6", 60), new ChartPoint("W7", 64), new ChartPoint("W8", 68)
            );
            default -> List.of(
                new ChartPoint("W1", 46), new ChartPoint("W2", 51), new ChartPoint("W3", 55), new ChartPoint("W4", 58),
                new ChartPoint("W5", 61), new ChartPoint("W6", 64), new ChartPoint("W7", 66), new ChartPoint("W8", 69)
            );
        };
    }

    private String renderAreaChart(List<ChartPoint> points) {
        if (points == null || points.isEmpty()) {
            return "";
        }

        int left = 16;
        int right = 424;
        int top = 16;
        int bottom = 116;

        double step = points.size() > 1 ? (right - left) / (double) (points.size() - 1) : 0;

        StringBuilder polyline = new StringBuilder();
        StringBuilder area = new StringBuilder();

        int firstX = left;
        int lastX = left;

        for (int i = 0; i < points.size(); i++) {
            ChartPoint point = points.get(i);
            int x = left + (int) Math.round(i * step);
            int y = bottom - (int) Math.round((point.value() / 100.0) * (bottom - top));

            if (i == 0) {
                area.append("M").append(x).append(",").append(y);
                firstX = x;
            } else {
                area.append(" L").append(x).append(",").append(y);
            }

            polyline.append(x).append(",").append(y).append(" ");
            lastX = x;
        }

        area.append(" L").append(lastX).append(",").append(bottom)
            .append(" L").append(firstX).append(",").append(bottom)
            .append(" Z");

        int skip = points.size() > 8 ? 2 : 1;
        StringBuilder labels = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            if (i % skip == 0 || i == points.size() - 1) {
                int x = left + (int) Math.round(i * step);
                labels.append("<text x='").append(x)
                    .append("' y='128' text-anchor='middle' font-size='9' fill='var(--ink3)' font-family='inherit'>")
                    .append(points.get(i).label())
                    .append("</text>");
            }
        }

        int highlightY = bottom - (int) Math.round((points.get(points.size() - 1).value() / 100.0) * (bottom - top));

        return """
            <svg class='chart-svg' viewBox='0 0 440 130' xmlns='http://www.w3.org/2000/svg'>
              <line x1='16' y1='116' x2='424' y2='116' stroke='var(--line)' stroke-width='1'/>
              <line x1='16' y1='82' x2='424' y2='82' stroke='var(--line)' stroke-width='1' stroke-dasharray='3 4'/>
              <line x1='16' y1='48' x2='424' y2='48' stroke='var(--line)' stroke-width='1' stroke-dasharray='3 4'/>
              <line x1='16' y1='16' x2='424' y2='16' stroke='var(--line)' stroke-width='1' stroke-dasharray='3 4'/>
              <path d='%s' fill='var(--spot-bg)'/>
              <polyline points='%s' fill='none' stroke='var(--spot)' stroke-width='2.2' stroke-linecap='round' stroke-linejoin='round'/>
              <circle cx='%s' cy='%s' r='3.5' fill='var(--spot)'/>
              %s
            </svg>
        """.formatted(area, polyline.toString().trim(), lastX, highlightY, labels);
    }

    private Div buildStat(String label, String value, String delta, String tone) {
        Div stat = new Div();
        stat.addClassName("stat");

        Span labelEl = new Span(label);
        labelEl.addClassName("stat-label");

        Span valueEl = new Span(value);
        valueEl.addClassName("stat-val");

        Span deltaEl = new Span(delta);
        deltaEl.addClassNames("stat-delta", tone);

        stat.add(labelEl, valueEl, deltaEl);
        return stat;
    }

    private CardShell buildCard(String title, String hint, Button action) {
        Div card = new Div();
        card.addClassName("card");

        Div header = new Div();
        header.addClassName("card-h");

        Span titleEl = new Span(title);
        titleEl.addClassName("card-title");
        header.add(titleEl);

        if (action != null) {
            header.add(action);
        } else if (hint != null && !hint.isBlank()) {
            Span hintEl = new Span(hint);
            hintEl.addClassName("card-hint");
            header.add(hintEl);
        }

        Div body = new Div();
        body.addClassName("card-b");

        card.add(header, body);
        return new CardShell(card, body);
    }

    private Div buildTrafficSources() {
        Div wrapper = new Div();
        wrapper.addClassName("ring-wrap");

        Div ring = new Div();
        ring.addClassName("traffic-ring");

        Span center = new Span("40%");
        center.addClassName("traffic-ring__center");
        ring.add(center);

        Div legend = new Div();
        legend.addClassName("ring-legend");
        legend.add(buildRingLegend("Organic", "40%", "ring-dot--spot"));
        legend.add(buildRingLegend("Direct", "24%", "ring-dot--muted"));
        legend.add(buildRingLegend("Other", "36%", "ring-dot--line"));

        wrapper.add(ring, legend);
        return wrapper;
    }

    private Div buildRingLegend(String label, String value, String dotClass) {
        Div row = new Div();
        row.addClassName("ring-leg");

        Div left = new Div();
        left.addClassName("ring-leg-left");

        Span dot = new Span();
        dot.addClassNames("ring-dot", dotClass);

        Span labelSpan = new Span(label);
        left.add(dot, labelSpan);

        Span valueSpan = new Span(value);
        valueSpan.addClassName("ring-val");

        row.add(left, valueSpan);
        return row;
    }

    private Grid<OrderRow> buildRecentOrdersGrid() {
        Grid<OrderRow> grid = new Grid<>(OrderRow.class, false);
        grid.addClassName("table-grid");
        grid.setWidthFull();
        grid.setAllRowsVisible(true);

        grid.addColumn(OrderRow::id).setHeader("Order").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(OrderRow::customer).setHeader("Customer").setAutoWidth(true);
        grid.addColumn(OrderRow::amount).setHeader("Amount").setAutoWidth(true);
        grid.addComponentColumn(order -> buildStatusBadge(order.status())).setHeader("Status").setAutoWidth(true);

        grid.setItems(
            new OrderRow("#NX-9012", "Alex Wong", "$1,199", "Paid"),
            new OrderRow("#NX-9011", "Sarah Kim", "$49", "Pending"),
            new OrderRow("#NX-9010", "Lior Maron", "$4,999", "Paid"),
            new OrderRow("#NX-9009", "Dani Park", "$0", "Trial"),
            new OrderRow("#NX-9008", "Marcus Bell", "$49", "Failed")
        );

        return grid;
    }

    private Span buildStatusBadge(String status) {
        Span badge = new Span(status);
        badge.addClassName("status-pill");
        switch (status.toLowerCase()) {
            case "paid" -> badge.addClassName("status-pill--green");
            case "pending" -> badge.addClassName("status-pill--amber");
            case "failed" -> badge.addClassName("status-pill--red");
            default -> badge.addClassName("status-pill--gray");
        }
        return badge;
    }

    private Div buildActivityFeed(List<User> recentUsers) {
        Div feed = new Div();
        feed.addClassName("feed");

        if (recentUsers.isEmpty()) {
            Span empty = new Span("No recent activity yet.");
            empty.addClassName("empty");
            feed.add(empty);
            return feed;
        }

        for (int i = 0; i < recentUsers.size(); i++) {
            User user = recentUsers.get(i);

            Div item = new Div();
            item.addClassName("feed-item");

            Span dot = new Span();
            dot.addClassName("feed-dot");
            if (i < 2) {
                dot.addClassName("hi");
            }

            Div textWrap = new Div();

            Span text = new Span(user.getFullName() + " joined as " + user.getRole().name());
            text.addClassName("feed-text");

            Span time = new Span(user.getCreatedAt().format(FEED_DATE_FMT));
            time.addClassName("feed-time");

            textWrap.add(text, time);
            item.add(dot, textWrap);
            feed.add(item);
        }

        return feed;
    }

    private record CardShell(Div card, Div body) {
    }

    private record OrderRow(String id, String customer, String amount, String status) {
    }

    private record ChartPoint(String label, int value) {
    }

    private record BarDatum(String day, int orders, int revenue) {
    }
}

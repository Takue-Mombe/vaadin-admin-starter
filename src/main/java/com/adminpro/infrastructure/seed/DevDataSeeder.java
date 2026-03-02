package com.adminpro.infrastructure.seed;

import com.adminpro.domain.AnalyticsMetric;
import com.adminpro.domain.OrderRecord;
import com.adminpro.domain.ProductItem;
import com.adminpro.domain.ReportConfig;
import com.adminpro.domain.Role;
import com.adminpro.domain.SupportMessage;
import com.adminpro.domain.User;
import com.adminpro.domain.UserSettings;
import com.adminpro.infrastructure.repo.AnalyticsMetricRepository;
import com.adminpro.infrastructure.repo.OrderRecordRepository;
import com.adminpro.infrastructure.repo.ProductItemRepository;
import com.adminpro.infrastructure.repo.ReportConfigRepository;
import com.adminpro.infrastructure.repo.SupportMessageRepository;
import com.adminpro.infrastructure.repo.UserRepository;
import com.adminpro.infrastructure.repo.UserSettingsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds realistic demo data into H2 on startup.
 * Guards against re-seeding so it is safe to restart the application.
 */
@Component
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final UserSettingsRepository settingsRepo;
    private final AnalyticsMetricRepository analyticsRepo;
    private final OrderRecordRepository orderRepo;
    private final ProductItemRepository productRepo;
    private final SupportMessageRepository messageRepo;
    private final ReportConfigRepository reportRepo;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepo,
                         UserSettingsRepository settingsRepo,
                         AnalyticsMetricRepository analyticsRepo,
                         OrderRecordRepository orderRepo,
                         ProductItemRepository productRepo,
                         SupportMessageRepository messageRepo,
                         ReportConfigRepository reportRepo,
                         PasswordEncoder passwordEncoder) {
        this.userRepo    = userRepo;
        this.settingsRepo = settingsRepo;
        this.analyticsRepo = analyticsRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.messageRepo = messageRepo;
        this.reportRepo = reportRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedSettings();
        seedAnalytics();
        seedOrders();
        seedProducts();
        seedMessages();
        seedReports();
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    private void seedUsers() {
        if (userRepo.count() > 0) return;

        String defaultPasswordHash = passwordEncoder.encode("admin123");

        userRepo.saveAll(List.of(
            new User("Alice",   "Müller",   "alice.muller@adminpro.io",   Role.ADMIN,   true, defaultPasswordHash),
            new User("Bob",     "Johnson",  "bob.johnson@adminpro.io",    Role.MANAGER, true, defaultPasswordHash),
            new User("Carla",   "Fernández","carla.fernandez@adminpro.io",Role.MANAGER, true, defaultPasswordHash),
            new User("David",   "Kim",      "david.kim@adminpro.io",      Role.VIEWER,  true, defaultPasswordHash),
            new User("Eva",     "Schmidt",  "eva.schmidt@adminpro.io",    Role.VIEWER,  true, defaultPasswordHash),
            new User("Frank",   "Nakamura", "frank.nakamura@adminpro.io", Role.VIEWER,  true, defaultPasswordHash),
            new User("Grace",   "Okonkwo",  "grace.okonkwo@adminpro.io",  Role.MANAGER, false, defaultPasswordHash),
            new User("Henry",   "Liu",      "henry.liu@adminpro.io",      Role.VIEWER,  true, defaultPasswordHash),
            new User("Ingrid",  "Berger",   "ingrid.berger@adminpro.io",  Role.VIEWER,  false, defaultPasswordHash),
            new User("James",   "Patel",    "james.patel@adminpro.io",    Role.ADMIN,   true, defaultPasswordHash)
        ));
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    private void seedSettings() {
        if (settingsRepo.count() > 0) return;
        settingsRepo.save(new UserSettings());
    }

    private void seedAnalytics() {
        if (analyticsRepo.count() > 0) return;

        analyticsRepo.saveAll(List.of(
            new AnalyticsMetric("Page Views", new BigDecimal("248.4"), "K", "This week", "UP"),
            new AnalyticsMetric("Avg Session", new BigDecimal("4.62"), "min", "This week", "UP"),
            new AnalyticsMetric("Bounce Rate", new BigDecimal("38.2"), "%", "This week", "DOWN"),
            new AnalyticsMetric("Orders", new BigDecimal("2.84"), "K", "This month", "UP")
        ));
    }

    private void seedOrders() {
        if (orderRepo.count() > 0) return;

        orderRepo.saveAll(List.of(
            new OrderRecord("NX-9012", "Alex Wong", "Pro Annual", new BigDecimal("1199.00"), "PAID", LocalDate.now()),
            new OrderRecord("NX-9011", "Sarah Kim", "Starter", new BigDecimal("49.00"), "PENDING", LocalDate.now()),
            new OrderRecord("NX-9010", "Lior Maron", "Enterprise", new BigDecimal("4999.00"), "PAID", LocalDate.now().minusDays(1)),
            new OrderRecord("NX-9009", "Dani Park", "Trial", new BigDecimal("0.00"), "TRIAL", LocalDate.now().minusDays(1)),
            new OrderRecord("NX-9008", "Marcus Bell", "Starter", new BigDecimal("49.00"), "FAILED", LocalDate.now().minusDays(2))
        ));
    }

    private void seedProducts() {
        if (productRepo.count() > 0) return;

        productRepo.saveAll(List.of(
            new ProductItem("Arc Starter", "ARC-ST-001", "Subscription", new BigDecimal("49.00"), 240, "ACTIVE"),
            new ProductItem("Arc Pro Annual", "ARC-PR-AN", "Subscription", new BigDecimal("1199.00"), 96, "ACTIVE"),
            new ProductItem("Enterprise Seat", "ARC-ENT-01", "License", new BigDecimal("4999.00"), 24, "ACTIVE"),
            new ProductItem("Support Add-on", "ARC-SUP-10", "Service", new BigDecimal("299.00"), 9, "DRAFT")
        ));
    }

    private void seedMessages() {
        if (messageRepo.count() > 0) return;

        messageRepo.saveAll(List.of(
            new SupportMessage("Sarah Kim", "sarah@startup.co", "Need account approval", "Please review my enterprise signup.", "Email", "UNREAD", LocalDateTime.now().minusMinutes(12)),
            new SupportMessage("Marcus Bell", "m.bell@legacy.org", "Payment retry request", "Card failed on renewal, can you retry?", "Chat", "OPEN", LocalDateTime.now().minusHours(2)),
            new SupportMessage("Alex Wong", "alex@techcorp.com", "Invoice copy", "Need invoice PDF for accounting.", "Email", "CLOSED", LocalDateTime.now().minusDays(1))
        ));
    }

    private void seedReports() {
        if (reportRepo.count() > 0) return;

        reportRepo.saveAll(List.of(
            new ReportConfig("Monthly Revenue", "Jordan Lee", "Monthly", "READY", LocalDate.now().minusDays(2)),
            new ReportConfig("User Growth", "Jordan Lee", "Weekly", "RUNNING", LocalDate.now().minusDays(1)),
            new ReportConfig("Billing Exceptions", "Finance Team", "Daily", "FAILED", LocalDate.now().minusDays(3))
        ));
    }
}

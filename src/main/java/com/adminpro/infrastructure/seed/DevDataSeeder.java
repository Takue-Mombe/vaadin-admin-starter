package com.adminpro.infrastructure.seed;

import com.adminpro.domain.Role;
import com.adminpro.domain.User;
import com.adminpro.domain.UserSettings;
import com.adminpro.infrastructure.repo.UserRepository;
import com.adminpro.infrastructure.repo.UserSettingsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds realistic demo data into H2 on startup.
 * Guards against re-seeding so it is safe to restart the application.
 */
@Component
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final UserSettingsRepository settingsRepo;

    public DevDataSeeder(UserRepository userRepo, UserSettingsRepository settingsRepo) {
        this.userRepo    = userRepo;
        this.settingsRepo = settingsRepo;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedSettings();
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    private void seedUsers() {
        if (userRepo.count() > 0) return;

        userRepo.saveAll(List.of(
            new User("Alice",   "Müller",   "alice.muller@adminpro.io",   Role.ADMIN,   true),
            new User("Bob",     "Johnson",  "bob.johnson@adminpro.io",    Role.MANAGER, true),
            new User("Carla",   "Fernández","carla.fernandez@adminpro.io",Role.MANAGER, true),
            new User("David",   "Kim",      "david.kim@adminpro.io",      Role.VIEWER,  true),
            new User("Eva",     "Schmidt",  "eva.schmidt@adminpro.io",    Role.VIEWER,  true),
            new User("Frank",   "Nakamura", "frank.nakamura@adminpro.io", Role.VIEWER,  true),
            new User("Grace",   "Okonkwo",  "grace.okonkwo@adminpro.io",  Role.MANAGER, false),
            new User("Henry",   "Liu",      "henry.liu@adminpro.io",      Role.VIEWER,  true),
            new User("Ingrid",  "Berger",   "ingrid.berger@adminpro.io",  Role.VIEWER,  false),
            new User("James",   "Patel",    "james.patel@adminpro.io",    Role.ADMIN,   true)
        ));
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    private void seedSettings() {
        if (settingsRepo.count() > 0) return;
        settingsRepo.save(new UserSettings());
    }
}

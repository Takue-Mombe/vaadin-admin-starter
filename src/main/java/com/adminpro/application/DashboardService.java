package com.adminpro.application;

import com.adminpro.domain.DashboardStat;
import com.adminpro.domain.Role;
import com.adminpro.domain.User;
import com.adminpro.infrastructure.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides data for the Dashboard view.
 * All stat tiles are computed from live DB queries – replace with
 * real business metrics as the project grows.
 */
@Service
public class DashboardService {

    private final UserRepository userRepo;

    public DashboardService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<DashboardStat> getStats() {
        long total   = userRepo.count();
        long active  = userRepo.countByActiveTrue();
        long admins  = userRepo.countByRole(Role.ADMIN);
        long managers = userRepo.countByRole(Role.MANAGER);

        return List.of(
            new DashboardStat("Total Users",    String.valueOf(total),    "all accounts",      "vaadin:users"),
            new DashboardStat("Active Users",   String.valueOf(active),   "currently enabled", "vaadin:check-circle"),
            new DashboardStat("Administrators", String.valueOf(admins),   "full access",       "vaadin:shield"),
            new DashboardStat("Managers",       String.valueOf(managers), "team leads",        "vaadin:group")
        );
    }

    /** Returns the 5 most recently created users for the "Recent Activity" table. */
    public List<User> getRecentActivity() {
        return userRepo.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();
    }
}

package com.adminpro.application;

import com.adminpro.domain.UserSettings;
import com.adminpro.infrastructure.repo.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the single application-settings row (id = 1).
 */
@Service
@Transactional
public class SettingsService {

    private static final long SETTINGS_ID = 1L;

    private final UserSettingsRepository repo;

    public SettingsService(UserSettingsRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public UserSettings load() {
        return repo.findById(SETTINGS_ID)
                .orElseGet(UserSettings::new);
    }

    public UserSettings save(UserSettings settings) {
        return repo.save(settings);
    }
}

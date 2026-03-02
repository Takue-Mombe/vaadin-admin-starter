package com.adminpro.application;

import com.adminpro.domain.User;
import com.adminpro.infrastructure.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CRUD operations for the Users view.
 */
@Service
@Transactional
public class UserService {

    public static final String DEFAULT_PASSWORD = "admin123";

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repo.findAll();
    }

    /**
     * Returns all users whose first name, last name, or email contains the
     * given filter string (case-insensitive). If the filter is blank, returns all.
     */
    @Transactional(readOnly = true)
    public List<User> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return repo.findAll();
        }
        String f = filter.trim();
        return repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(f, f, f);
    }

    public User save(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        }
        return repo.save(user);
    }

    public void delete(User user) {
        repo.delete(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return repo.findByEmailIgnoreCase(email.trim());
    }

    public User updatePassword(User user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return repo.save(user);
    }
}

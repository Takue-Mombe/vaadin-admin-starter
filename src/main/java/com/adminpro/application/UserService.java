package com.adminpro.application;

import com.adminpro.domain.User;
import com.adminpro.infrastructure.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD operations for the Users view.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
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
        return repo.save(user);
    }

    public void delete(User user) {
        repo.delete(user);
    }
}

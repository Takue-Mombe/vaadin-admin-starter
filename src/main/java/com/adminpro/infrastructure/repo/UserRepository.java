package com.adminpro.infrastructure.repo;

import com.adminpro.domain.User;
import com.adminpro.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Full-text filter used by UsersView. */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    long countByActiveTrue();

    long countByRole(Role role);
}

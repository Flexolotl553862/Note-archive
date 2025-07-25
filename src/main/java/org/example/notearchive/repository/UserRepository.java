package org.example.notearchive.repository;

import jakarta.validation.constraints.NotNull;
import org.example.notearchive.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByName(String name);

    boolean existsByLogin(String login);

    Optional<User> findByLogin(String login);

    Optional<User> findByLoginOrEmail(String login, String email);

    Iterable<User> findByRoleIn(Collection<User.@NotNull Role> role);
}

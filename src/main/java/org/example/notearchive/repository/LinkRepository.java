package org.example.notearchive.repository;

import org.example.notearchive.domain.Link;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {

    Optional<Link> findByLink(String link);

    boolean existsByLink(String link);
}

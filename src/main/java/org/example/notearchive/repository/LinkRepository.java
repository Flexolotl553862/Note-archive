package org.example.notearchive.repository;

import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {
    Iterable<Link> findByAuthorAndNote(User author, Note note);

    Optional<Link> findByLink(String link);
}

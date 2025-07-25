package org.example.notearchive.repository;

import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {
    Iterable<Link> findByNote(Note note);
}

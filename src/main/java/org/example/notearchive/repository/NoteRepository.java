package org.example.notearchive.repository;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends CrudRepository<Note, Long> {

    boolean existsNoteByTitle(String title);

    List<Note> findAllByAuthor_Login(String login);
}

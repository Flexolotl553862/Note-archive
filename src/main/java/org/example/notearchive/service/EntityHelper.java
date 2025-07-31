package org.example.notearchive.service;

import com.google.api.client.util.Lists;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.repository.LinkRepository;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.example.notearchive.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class EntityHelper {
    private final NoteRepository noteRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;

    public EntityHelper(
            NoteRepository noteRepository,
            StorageEntryRepository storageEntryRepository,
            UserRepository userRepository,
            LinkRepository linkRepository
    ) {
        this.noteRepository = noteRepository;
        this.storageEntryRepository = storageEntryRepository;
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
    }

    public Note getNote(Long id) {
        return noteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such note")
        );
    }

    public StorageEntry getEntry(Long id) {
        return storageEntryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such entry")
        );
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such user")
        );
    }

    public Link getLink(Long id) {
        return linkRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No such link")
        );
    }

    public List<User> getWriters() {
        return Lists.newArrayList(userRepository.findByRole(User.Role.ROLE_WRITER).iterator());
    }
}

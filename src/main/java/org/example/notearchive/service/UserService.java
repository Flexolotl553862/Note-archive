package org.example.notearchive.service;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.RegisterForm;
import org.example.notearchive.repository.LinkRepository;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.example.notearchive.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final LinkRepository linkRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NoteRepository noteRepository,
            StorageEntryRepository repository,
            LinkRepository linkRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.noteRepository = noteRepository;
        this.storageEntryRepository = repository;
        this.linkRepository = linkRepository;
    }

    public User Register(RegisterForm registerForm) {
        User user = new User();
        user.setLogin(registerForm.getLogin());
        user.setRole(User.Role.ROLE_READER);
        user.setPasswordSha(passwordEncoder.encode(registerForm.getPassword()));
        user.setName(registerForm.getFullName());
        user.setEmail(registerForm.getEmail());
        userRepository.save(user);
        return user;
    }

    public boolean isNoteEditor(long noteId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        Note note = noteRepository.findById(noteId).orElse(null);
        return note != null && (user.getRole().equals(User.Role.ROLE_ADMIN)
                || (note.getEditors() != null && note.getEditors().contains(user)));
    }

    public boolean isLinkAuthor(long linkId, Authentication authentication) {
        return linkRepository.findById(linkId).orElseThrow().getAuthor().getId().equals(
                ((User) authentication.getPrincipal()).getId()
        );
    }

    public boolean isNoteAuthor(long noteId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        Note note = noteRepository.findById(noteId).orElse(null);
        return note != null && (user.getRole().equals(User.Role.ROLE_WRITER) && note.getAuthor().equals(user))
                || user.getRole().equals(User.Role.ROLE_ADMIN);
    }

    public boolean canChangeEntry(long entryId, Authentication authentication) {
        StorageEntry entry = storageEntryRepository.findById(entryId).orElse(null);
        return entry != null && isNoteEditor(entry.getParentNote().getId(), authentication);
    }
}

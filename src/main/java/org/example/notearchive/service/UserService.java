package org.example.notearchive.service;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.RegisterForm;
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

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NoteRepository noteRepository,
            StorageEntryRepository repository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.noteRepository = noteRepository;
        this.storageEntryRepository = repository;
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

    public boolean isNoteAuthor(long noteId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        Note note = noteRepository.findById(noteId).orElse(null);
        return note != null
                && (user.getRole().equals(User.Role.ROLE_WRITER) || user.getRole().equals(User.Role.ROLE_ADMIN))
                && note.getAuthor().equals(user);
    }

    public boolean canChangeEntry(long entryId, Authentication authentication) {
        StorageEntry entry = storageEntryRepository.findById(entryId).orElse(null);
        return entry != null && isNoteAuthor(entry.getParentNote().getId(), authentication);
    }
}

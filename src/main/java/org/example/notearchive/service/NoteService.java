package org.example.notearchive.service;

import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.NoteForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final StorageEntryService storageEntryService;
    private final FileStorage fileStorage;
    private final AIService aiService;
    private final StorageEntryRepository storageEntryRepository;

    public void addNote(NoteForm noteForm, User author) throws StorageException {
        Note note = new Note(
                noteForm.getTitle(),
                noteForm.getStartSemester(),
                noteForm.getEndSemester(),
                author
        );
        StorageEntry entry = new StorageEntry(
                noteForm.getTitle(),
                File.separator + noteForm.getTitle(),
                StorageEntry.ENTRY_TYPE.DIRECTORY,
                note
        );
        storageEntryRepository.save(entry);
        note.setContent(entry);
        if (!fileStorage.tryToSaveAsNestedFolders(noteForm.getFile(), entry)) {
            fileStorage.saveAsFile(noteForm.getFile(), entry);
        }
        storageEntryService.createEntry(entry, noteForm.getFile().getOriginalFilename(), StorageEntry.ENTRY_TYPE.FILE);
        noteRepository.save(note);
    }

    public void deleteNote(Note note) {
        noteRepository.delete(note);
    }

    public String getMarkdownDescription(Note note) throws StorageException {
        try {
            StorageEntry descriptionEntry = note
                    .getContent()
                    .getChildren()
                    .stream()
                    .filter((entry) -> entry.getName().equals("description.md"))
                    .findFirst()
                    .orElseThrow();

            File descriptionFile = fileStorage.getEntryContent(descriptionEntry);
            return Files.readString(descriptionFile.toPath());
        } catch (Exception e) {
            throw new StorageException("Could not find description markdown file.", e);
        }
    }

    public String addMarkdownDescription(Note note, String userDescription) throws StorageException {
        String question = "title : " + note.getTitle() +
                "startSemester : " + note.getStartSemester() +
                "endSemester : " + note.getEndSemester() +
                "description from user: " + userDescription;
        String description;
        try {
            description = aiService.generateMarkdown(question);
        } catch (Exception e) {
            throw new StorageException("Could not generate markdown", e);
        }
        String name = "description.md";
        fileStorage.saveAsFile(new ByteArrayInputStream(description.getBytes()), name, note.getContent());
        storageEntryService.createEntry(note.getContent(), name, StorageEntry.ENTRY_TYPE.FILE);
        return description;
    }

    public void changeEditorState(Note note, User editor, boolean state) {
        Set<User> editors = note.getEditors() == null ? new HashSet<>() : note.getEditors();
        if (state) {
            editors.add(editor);
        } else {
            editors.remove(editor);
        }
        note.setEditors(editors);
        noteRepository.save(note);
    }

    public boolean isNoteEditor(Note note, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        return user.getRole().equals(User.Role.ROLE_ADMIN)
                || (note.getEditors() != null && note.getEditors().contains(user));
    }

    public boolean isNoteAuthor(Note note, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        return (user.getRole().equals(User.Role.ROLE_WRITER) && note.getAuthor().equals(user))
                || user.getRole().equals(User.Role.ROLE_ADMIN);
    }
}
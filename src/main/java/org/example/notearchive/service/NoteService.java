package org.example.notearchive.service;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.NoteForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final FileStorage fileStorage;
    private final AIService aiService;

    public NoteService(
            NoteRepository noteRepository,
            StorageEntryRepository storageEntryRepository,
            FileStorage fileStorage,
            AIService aiService
    ) {
        this.noteRepository = noteRepository;
        this.storageEntryRepository = storageEntryRepository;
        this.fileStorage = fileStorage;
        this.aiService = aiService;
    }

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
        note.setContent(entry);
        if (!fileStorage.tryToSaveAsNestedFolders(noteForm.getFile(), entry)) {
            fileStorage.saveAsFile(noteForm.getFile(), entry);
        }
        noteRepository.save(note);
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

        String description = aiService.generateMarkdown(question);
        fileStorage.saveAsFile(
                new ByteArrayInputStream(description.getBytes()),
                "description.md",
                note.getContent()
        );
        storageEntryRepository.save(note.getContent());
        return description;
    }

    public void changeEditors(Note note, User editor, boolean state) {
        Set<User> editors = note.getEditors() == null ? new HashSet<>() : note.getEditors();
        if (state) {
            editors.add(editor);
        } else {
            editors.remove(editor);
        }
        note.setEditors(editors);
        noteRepository.save(note);
    }
}
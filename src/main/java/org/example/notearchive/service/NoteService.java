package org.example.notearchive.service;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.NoteForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NoteService {
    private final StorageService storageService;
    private final NoteRepository noteRepository;

    public NoteService(StorageService storageService, NoteRepository noteRepository) {
        this.storageService = storageService;
        this.noteRepository = noteRepository;
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
        storageService.uploadMultipartFile(noteForm.getFile(), entry, true);
        noteRepository.save(note);
    }
}

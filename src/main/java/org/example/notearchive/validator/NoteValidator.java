package org.example.notearchive.validator;

import lombok.NonNull;
import org.example.notearchive.dto.NoteForm;
import org.example.notearchive.repository.NoteRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;

@Component
public class NoteValidator implements Validator {
    private final NoteRepository noteRepository;
    private final int MAX_WORD_LENGTH = 14;

    public NoteValidator(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(NoteForm.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        NoteForm noteForm = (NoteForm) target;
        Arrays.stream(noteForm.getTitle().split("\\s+")).map(String::length).max(Integer::compareTo).ifPresent(
                (len) -> {
                    if (len > MAX_WORD_LENGTH) {
                        errors.rejectValue("title", "note.titleTooLong", "Title too long for card frame");
                    }
                }
        );
        if (noteForm.getFile() == null || noteForm.getFile().isEmpty()) {
            errors.rejectValue("file", "note.file.empty", "must not be empty");
        }
        if (noteForm.getStartSemester() > noteForm.getEndSemester()) {
            errors.rejectValue("startSemester", "note.startSemester.greater", "must be equal or less than end semester");
            errors.rejectValue("endSemester", "note.endSemester.less", "must be equal or greater than start semester");
        }
        if (noteRepository.existsNoteByTitle(noteForm.getTitle())) {
            errors.rejectValue("title", "note.already.exists", "note already exists");
        }
    }
}

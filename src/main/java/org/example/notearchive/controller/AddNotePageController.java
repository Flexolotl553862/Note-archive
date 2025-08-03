package org.example.notearchive.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.NoteForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.service.NoteService;
import org.example.notearchive.validator.NoteValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequiredArgsConstructor
public class AddNotePageController {
    private final NoteValidator noteValidator;
    private final NoteService noteService;

    @ModelAttribute
    public void initAll(Model model, Authentication authentication) {
        model.addAttribute("noteForm", new NoteForm());
        if (authentication != null) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/add/note")
    @PreAuthorize("hasAnyRole('ROLE_WRITER', 'ROLE_ADMIN')")
    public String addNote() {
        return "add-note";
    }

    @PostMapping("/add/note")
    @PreAuthorize("hasAnyRole('ROLE_WRITER', 'ROLE_ADMIN')")
    public String addNote(
            @Valid @ModelAttribute("noteForm") NoteForm noteForm,
            BindingResult bindingResult,
            SessionStatus sessionStatus,
            Authentication authentication
    ) {
        noteValidator.validate(noteForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "add-note";
        }
        try {
            noteService.addNote(noteForm, (User) authentication.getPrincipal());
        } catch (StorageException e) {
            bindingResult.rejectValue("file", "add.note.error", e.getMessage());
            return "add-note";
        }
        sessionStatus.setComplete();
        return "redirect:/home";
    }
}

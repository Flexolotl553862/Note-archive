package org.example.notearchive.controller;

import jakarta.validation.Valid;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.NoteForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.repository.UserRepository;
import org.example.notearchive.service.NoteService;
import org.example.notearchive.validator.NoteValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes("user")
public class AddNotePageController {
    private final NoteValidator noteValidator;
    private final NoteService noteService;
    private final UserRepository userRepository;

    public AddNotePageController(NoteValidator noteValidator, NoteService noteService, UserRepository userRepository) {
        this.noteValidator = noteValidator;
        this.noteService = noteService;
        this.userRepository = userRepository;
    }

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
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (bindingResult.hasErrors()) {
            return "add-note";
        }
        noteValidator.validate(noteForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "add-note";
        }
        try {
            User user = userRepository.findByLogin(userDetails.getUsername()).orElse(null);
            noteService.addNote(noteForm, user);
        } catch (StorageException e) {
            bindingResult.rejectValue("file", "add.note.error", e.getMessage());
            return "add-note";
        }
        sessionStatus.setComplete();
        return "redirect:/home";
    }
}

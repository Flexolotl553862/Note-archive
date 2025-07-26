package org.example.notearchive.controller;

import com.google.api.client.util.Lists;
import jakarta.validation.Valid;
import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.dto.RenewLinkForm;
import org.example.notearchive.repository.LinkRepository;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.UserRepository;
import org.example.notearchive.validator.LinkValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
public class EditNoteController {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final LinkValidator linkValidator;

    public EditNoteController(
            NoteRepository noteRepository,
            UserRepository userRepository,
            LinkRepository linkRepository,
            LinkValidator linkValidator
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
        this.linkValidator = linkValidator;
    }

    @ModelAttribute
    public void initAll(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/note/{id}/editors")
    @PreAuthorize("@userService.isNoteAuthor(#id, authentication)")
    public String editNotePage(@PathVariable("id") long id, Model model, Authentication authentication) {
        noteRepository.findById(id).ifPresent((note) -> {
            List<User> editors = Lists.newArrayList(
                    userRepository.findByRole(User.Role.ROLE_WRITER).iterator()
            );
            if (authentication != null && authentication.getPrincipal() != null) {
                editors.remove((User) authentication.getPrincipal());
            }
            model.addAttribute("editors", editors);
            model.addAttribute("note", note);
        });
        return "note-editors";
    }

    @PostMapping("/note/change/editors")
    @PreAuthorize("@userService.isNoteAuthor(#noteId, authentication)")
    public ResponseEntity<Void> changeEditorList(
            @RequestParam("noteId") long noteId,
            @RequestParam("userId") long userId,
            @RequestParam("state") boolean state
    ) {

        Note note = noteRepository.findById(noteId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (note != null && user != null && note.getAuthor() != user) {
            Set<User> editors = note.getEditors();
            if (editors == null) {
                editors = new HashSet<>();
            }
            if (state) {
                editors.add(user);
            } else {
                editors.remove(user);
            }
            note.setEditors(editors);
            noteRepository.save(note);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/note/{id}/links")
    @PreAuthorize("@userService.isNoteEditor(#noteId, authentication)")
    public String getNoteLinks(@PathVariable("id") long noteId, Model model, Authentication authentication) {
        noteRepository.findById(noteId).ifPresent(
                note -> model.addAttribute(
                        "links",
                        linkRepository.findByAuthorAndNote((User) authentication.getPrincipal(), note)
                )
        );
        model.addAttribute("linkForm", new LinkForm());
        model.addAttribute("renewLinkForm", new RenewLinkForm());
        model.addAttribute("noteId", noteId);
        return "note-links";
    }

    @PostMapping("/generate/link")
    @PreAuthorize("@userService.isNoteEditor(#linkForm.noteId, authentication)")
    public ResponseEntity<Map<String, Object>> generateLink(
            @Valid @ModelAttribute LinkForm linkForm,
            BindingResult bindingResult,
            Authentication authentication
    ) {

        linkValidator.validate(linkForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "title", "Error!",
                    "text", bindingResult.getFieldError().getDefaultMessage()
            ));
        }
        Link link = new Link(
                UUID.randomUUID().toString(),
                linkForm.getDescription(),
                noteRepository.findById(linkForm.getNoteId()).orElseThrow(),
                linkForm.getDate(),
                (User) authentication.getPrincipal()
        );
        linkRepository.save(link);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "title", "Successfully created",
                "text", "Link has been created!"
        ));
    }

    @PostMapping("/delete/link")
    @PreAuthorize("@userService.isLinkAuthor(#linkId, authentication)")
    public ResponseEntity<Map<String, Object>> deleteLink(
            @RequestParam("linkId") Long linkId
    ) {
        linkRepository.delete(linkRepository
                .findById(linkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
        );
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "title", "Successfully deleted",
                "text", "Link has been deleted!"
        ));
    }

    @PostMapping("/renew/link")
    @PreAuthorize("@userService.isLinkAuthor(#renewLinkForm.linkId, authentication)")
    public ResponseEntity<Map<String, Object>> renewLink(
            @ModelAttribute RenewLinkForm renewLinkForm
    ) {
        if (renewLinkForm.getDate().before(new Date())) {
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "title", "Error!",
                    "text", "Could not set date in past."
            ));
        }
        Link link = linkRepository
                .findById(renewLinkForm.getLinkId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        link.setExpiryDate(renewLinkForm.getDate());
        linkRepository.save(link);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "title", "Successfully renewed",
                "text", "Link has been renewed!"
        ));
    }
}

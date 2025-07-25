package org.example.notearchive.controller;

import com.google.api.client.util.Lists;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.repository.LinkRepository;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class EditNoteController {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;

    public EditNoteController(NoteRepository noteRepository, UserRepository userRepository, LinkRepository linkRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
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
                    userRepository.findByRoleIn(List.of(User.Role.ROLE_WRITER, User.Role.ROLE_ADMIN)).iterator()
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
            @RequestParam("state") boolean state) {

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
    public String getNoteLinks(@PathVariable("id") long noteId, Model model) {
        noteRepository.findById(noteId).ifPresent(
                note -> model.addAttribute("links", linkRepository.findByNote(note))
        );
        model.addAttribute("linkForm", new LinkForm());
        return "note-links";
    }

    @PostMapping("/note/generate/link")
    @PreAuthorize("@userService.isNoteEditor(#noteId, authentication)")
    public String generateLink(@RequestParam("noteId") Long noteId, Model model) {
        return "note-links";
    }
}

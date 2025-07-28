package org.example.notearchive.controller;

import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.service.EntityHelper;
import org.example.notearchive.service.NoteService;
import org.example.notearchive.service.StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("note, user")
public class NotePageController {
    private final EntityHelper entityHelper;
    private final StorageService storageService;
    private final NoteService noteService;

    public NotePageController(
            StorageService storageService,
            NoteService noteService,
            EntityHelper entityHelper) {

        this.storageService = storageService;
        this.noteService = noteService;
        this.entityHelper = entityHelper;
    }

    @ModelAttribute
    public void initAll(Model model) {
        model.addAttribute("createDirectoryForm", new CreateDirectoryForm());
        model.addAttribute("fileForm", new FileForm());
    }

    @GetMapping("/note/{id}")
    public String note(@PathVariable long id) {
        return "redirect:/folder/" + entityHelper.getNote(id).getContent().getId();
    }

    @GetMapping("/note/{id}/description")
    public String noteDescription(@PathVariable long id, Model model) {
        model.addAttribute("note", entityHelper.getNote(id));
        return "note-description";
    }

    @GetMapping("/api/note/{id}/description")
    public ResponseEntity<String> getMarkdownDescription(@PathVariable long id) {
        String content;
        try {
            content = noteService.getMarkdownDescription(entityHelper.getNote(id));
        } catch (Exception ignored) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .body(content);
    }

    @PostMapping("/api/note/{id}/description/generate")
    @PreAuthorize("@userService.isNoteEditor(#id, authentication)")
    public ResponseEntity<String> generateDescription(@PathVariable long id, @RequestParam("data") String data) {
        try {
            return ResponseEntity.ok(noteService.addMarkdownDescription(entityHelper.getNote(id), data));
        } catch (StorageException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/folder/{id}")
    public String folder(@PathVariable long id, Model model) {
        StorageEntry folder = entityHelper.getEntry(id);
        model.addAllAttributes(Map.of(
                "path", storageService.getPath(folder),
                "folder", folder,
                "note", folder.getParentNote()
        ));
        return "note";
    }

    @GetMapping("/note/{id}/editors")
    @PreAuthorize("@userService.isNoteAuthor(#id, authentication)")
    public String editNotePage(@PathVariable("id") long id, Model model, Authentication authentication) {
        model.addAttribute("note", entityHelper.getNote(id));
        List<User> editors = entityHelper.getWriters();
        if (authentication != null && authentication.getPrincipal() != null) {
            editors.remove((User) authentication.getPrincipal());
        }
        model.addAttribute("editors", editors);
        return "note-editors";
    }

    @PostMapping("/note/change/editors")
    @PreAuthorize("@userService.isNoteAuthor(#noteId, authentication)")
    public ResponseEntity<Void> changeEditorList(
            @RequestParam("noteId") long noteId,
            @RequestParam("userId") long userId,
            @RequestParam("state") boolean state
    ) {
        noteService.changeEditors(entityHelper.getNote(noteId), entityHelper.getUser(userId), state);
        return ResponseEntity.ok().build();
    }
}

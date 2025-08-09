package org.example.notearchive.controller;

import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.service.NoteService;
import org.example.notearchive.service.StorageEntryService;
import org.example.notearchive.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@SessionAttributes("note, user")
@RequiredArgsConstructor
public class NotePageController {
    private final StorageEntryService storageEntryService;
    private final NoteService noteService;
    private final FileStorage fileStorage;
    private final ResponseHelper responseHelper;
    private final UserService userService;

    @ModelAttribute
    public void initAll(Model model) {
        model.addAttribute("createDirectoryForm", new CreateDirectoryForm());
        model.addAttribute("fileForm", new FileForm());
    }

    @GetMapping("/note/{note}")
    public String note(@PathVariable Note note) {
        return "redirect:/folder/" + note.getContent().getId();
    }

    @DeleteMapping("/delete/note")
    @PreAuthorize("@noteService.isNoteAuthor(#note, authentication)")
    public ResponseEntity<Map<String, Object>> deleteNote(@RequestParam("noteId") Note note) {
        try {
            fileStorage.deleteNoteContent(note);
            noteService.deleteNote(note);
        } catch (StorageException ignored) {
            return responseHelper.error("Could not delete note.");
        }
        return responseHelper.ok("Successfully deleted.", note.getTitle() + " has been deleted.");
    }

    @GetMapping("/note/{note}/description")
    public String noteDescription(@PathVariable Note note, Model model) {
        model.addAttribute("note", note);
        return "note-description";
    }

    @GetMapping("/api/note/{note}/description")
    public ResponseEntity<String> getMarkdownDescription(@PathVariable Note note) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body(noteService.getMarkdownDescription(note));
        } catch (Exception ignored) {}
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/api/note/{note}/description/generate")
    @PreAuthorize("@noteService.isNoteEditor(#note, authentication)")
    public ResponseEntity<String> generateDescription(
            @PathVariable Note note,
            @RequestParam("data") String userDescription
    ) {
        try {
            return ResponseEntity.ok(noteService.addMarkdownDescription(note, userDescription));
        } catch (StorageException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/folder/{folder}")
    @PreAuthorize("@storageEntryService.canOpenEntry(#folder, authentication)")
    public String folder(@PathVariable StorageEntry folder, Model model, Authentication authentication) {
        if (!folder.isDirectory()) {
            return "redirect:/not/found";
        }
        model.addAllAttributes(Map.of(
                "path", folder.getPathAsEntryList(),
                "folder", folder,
                "note", folder.getParentNote(),
                "children",
                folder.getChildren().stream().filter(e -> storageEntryService.canOpenEntry(e, authentication)).toList()
        ));
        return "note";
    }

    @GetMapping("/note/{note}/editors")
    @PreAuthorize("@noteService.isNoteAuthor(#note, authentication)")
    public String editNotePage(@PathVariable Note note, Model model, Authentication authentication) {
        model.addAttribute("note", note);
        model.addAttribute("editors", userService.getWritersExceptOne((User) authentication.getPrincipal()));
        return "note-editors";
    }

    @PatchMapping("/note/change/editors")
    @PreAuthorize("@noteService.isNoteAuthor(#note, authentication)")
    public ResponseEntity<Void> changeEditorList(
            @RequestParam("noteId") Note note,
            @RequestParam("userId") User user,
            @RequestParam("state") boolean state
    ) {
        noteService.changeEditorState(note, user, state);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/entry/set/lock")
    @PreAuthorize("@noteService.isNoteAuthor(#entry.parentNote, authentication)")
    public ResponseEntity<Map<String, Object>> setLock(
            @RequestParam("entryId") StorageEntry entry,
            @RequestParam("lock") Boolean lock
    ) {
        storageEntryService.setLock(entry, lock);
        return responseHelper.ok("Successfully updated!", "Entry has been " + (lock ? "locked" : "unlocked"));
    }
}

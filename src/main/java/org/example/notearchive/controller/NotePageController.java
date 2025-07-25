package org.example.notearchive.controller;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.example.notearchive.service.AIService;
import org.example.notearchive.service.StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Controller
@SessionAttributes("note, user")
public class NotePageController {
    private final NoteRepository noteRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final StorageService storageService;
    private final FileStorage fileStorage;
    private final AIService aiService;

    public NotePageController(
            NoteRepository noteRepository,
            StorageEntryRepository storageEntryRepository,
            StorageService storageService,
            AIService aiService,
            FileStorage fileStorage
    ) {
        this.noteRepository = noteRepository;
        this.storageEntryRepository = storageEntryRepository;
        this.storageService = storageService;
        this.aiService = aiService;
        this.fileStorage = fileStorage;
    }

    @ModelAttribute
    public void initAll(Model model, Authentication authentication) {
        model.addAttribute("createDirectoryForm", new CreateDirectoryForm());
        model.addAttribute("fileForm", new FileForm());
        if (authentication != null) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/note/{id}")
    public String note(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Optional<Note> note = noteRepository.findById(id);
        return note
                .map(value -> "redirect:/folder/" + value.getContent().getId())
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("message", "No such note");
                    return "redirect:/not/found";
                });
    }

    @GetMapping("/note/{id}/description")
    public String noteDescription(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Note> note = noteRepository.findById(id);
        if (note.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "No such note");
            return "redirect:/not/found";
        }
        model.addAttribute("note", note.get());
        return "note-description";
    }

    @GetMapping("/api/note/{id}/description")
    public ResponseEntity<String> getMarkdownDescription(@PathVariable long id) {
        Optional<Note> note = noteRepository.findById(id);
        String content;
        try {
            StorageEntry descriptionEntry = note
                    .orElseThrow()
                    .getContent()
                    .getChildren()
                    .stream()
                    .filter((entry) -> entry.getName().equals("description.md"))
                    .findFirst()
                    .orElseThrow();

            File descriptionFile = fileStorage.getEntryContent(descriptionEntry);
            content = Files.readString(descriptionFile.toPath());
        } catch (NullPointerException | NoSuchElementException | StorageException | IOException ignored) {
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
        Optional<Note> note = noteRepository.findById(id);
        try {
            Note notEmptyNote = note.orElseThrow(() -> new SecurityException("Note not found", null));
            if (notEmptyNote.getContent().getChildren().stream().anyMatch((entry) -> entry.getName().equals("description.md"))) {
                return ResponseEntity.badRequest().build();
            }
            String question = "title : " + notEmptyNote.getTitle() +
                    "startSemester : " + notEmptyNote.getStartSemester() +
                    "endSemester : " + notEmptyNote.getEndSemester() +
                    "description from user: " + data;

            String description = aiService.generateMarkdown(question);
            storageService.addMarkdownDescription(notEmptyNote, description);
            return ResponseEntity.ok(description);
        } catch (NullPointerException | StorageException ignored) {
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/folder/{id}")
    public String folder(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<StorageEntry> folder = storageEntryRepository.findById(id);
        return folder.map(curFolder -> {
            List<StorageEntry> path = new ArrayList<>();
            getPath(curFolder, path);
            model.addAttribute("path", path);
            model.addAttribute("folder", curFolder);
            model.addAttribute("note", folder.get().getParentNote());
            return "note";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("message", "No such folder");
            return "redirect:/not/found";
        });
    }

    private void getPath(StorageEntry entry, List<StorageEntry> path) {
        path.add(entry);
        if (entry.getParent() != null) {
            getPath(entry.getParent(), path);
        } else {
            Collections.reverse(path);
        }
    }
}

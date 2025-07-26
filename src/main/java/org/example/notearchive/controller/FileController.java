package org.example.notearchive.controller;

import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.example.notearchive.domain.Note;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.example.notearchive.service.StorageService;
import org.example.notearchive.validator.CreateDirectoryValidator;
import org.example.notearchive.validator.CreateFileValidator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class FileController {
    private final StorageService storageService;
    private final CreateDirectoryValidator createDirectoryValidator;
    private final CreateFileValidator createFileValidator;
    private final FileStorage fileStorage;
    private final StorageEntryRepository storageEntryRepository;
    private final NoteRepository noteRepository;

    public FileController(
            StorageService storageService,
            CreateDirectoryValidator createDirectoryValidator,
            CreateFileValidator createFileValidator, FileStorage fileStorage, StorageEntryRepository storageEntryRepository, NoteRepository noteRepository) {
        this.storageService = storageService;
        this.createDirectoryValidator = createDirectoryValidator;
        this.createFileValidator = createFileValidator;
        this.fileStorage = fileStorage;
        this.storageEntryRepository = storageEntryRepository;
        this.noteRepository = noteRepository;
    }

    @ModelAttribute
    public void initForms(Model model, Authentication authentication) {
        model.addAttribute("createDirectoryForm", new CreateDirectoryForm());
        model.addAttribute("fileForm", new FileForm());
        if (authentication != null) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> openFile(@PathVariable long id, RedirectAttributes redirectAttributes) {
        return getFileForResponse(id, "inline", redirectAttributes);
    }

    @GetMapping("/download/file/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable long id, RedirectAttributes redirectAttributes) {
        return getFileForResponse(id, "attachment", redirectAttributes);
    }

    @PostMapping("/delete/entry")
    @PreAuthorize("@userService.canChangeEntry(#entryId, authentication)")
    public ResponseEntity<Map<String, Object>> deleteEntry(
            @RequestParam("entryId") long entryId,
            @RequestParam("entryName") String entryName
    ) {
        try {
            fileStorage.deleteEntry(storageEntryRepository.findById(entryId).orElseThrow(
                    () -> new StorageException("Could not find entry", null)
            ));
        } catch (StorageException e) {
            return ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", "Could not delete " + entryName
                    )
            );
        }
        return ResponseEntity.ok(
                Map.of(
                        "ok", true,
                        "title", "Successfully deleted.",
                        "text", entryName + " has been deleted."
                )
        );
    }

    @PostMapping("/delete/note")
    @PreAuthorize("@userService.isNoteAuthor(#noteId, authentication)")
    public ResponseEntity<Map<String, Object>> deleteNote(@RequestParam("noteId") long noteId) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            return ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", "Could not find note."
                    ));
        }
        try {
            fileStorage.deleteNote(note);
        } catch (StorageException e) {
            return ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", "Could not delete note."
                    ));
        }
        return ResponseEntity.ok(
                Map.of(
                        "ok", true,
                        "title", "Successfully deleted.",
                        "text", note.getTitle() + " has been deleted."
                ));
    }

    @PostMapping("/folder/create")
    @PreAuthorize("@userService.canChangeEntry(#createDirectoryForm.parentId, authentication)")
    public ResponseEntity<Map<String, Object>> createDirectory(
            @Valid @ModelAttribute CreateDirectoryForm createDirectoryForm,
            BindingResult bindingResult
    ) {

        createDirectoryValidator.validate(createDirectoryForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", bindingResult.getFieldError().getDefaultMessage()
                    )
            );
        }
        try {
            fileStorage.createDirectory(
                    createDirectoryForm.getDirectoryName(),
                    storageEntryRepository.findById(createDirectoryForm.getParentId()).orElseThrow(
                            () -> new StorageException("Could not find parent entry", null)
                    )
            );
        } catch (StorageException ignored) {
            return ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", "Could not create " + createDirectoryForm.getDirectoryName() + "."
                    )
            );
        }
        return ResponseEntity.ok(
                Map.of(
                        "ok", true,
                        "title", "Successfully created.",
                        "text", createDirectoryForm.getDirectoryName() + " has been created."
                )
        );
    }

    @PostMapping("/file/create")
    @PreAuthorize("@userService.canChangeEntry(#fileForm.fileParentId, authentication)")
    public ResponseEntity<Map<String, Object>> createFile(
            @Valid @ModelAttribute FileForm fileForm,
            BindingResult bindingResult
    ) {
        createFileValidator.validate(fileForm, bindingResult);
        if (bindingResult.hasErrors()) {
            ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", bindingResult.getFieldError().getDefaultMessage()
                    ));
        }
        try {
            storageService.uploadMultipartFile(fileForm.getFileCreate(), fileForm.getFileParentId());
        } catch (StorageException ignored) {
            return ResponseEntity.ok(
                    Map.of(
                            "ok", false,
                            "title", "Error!",
                            "text", "Could not create " + fileForm.getFileCreate().getOriginalFilename() + "."
                    ));
        }
        return ResponseEntity.ok(
                Map.of(
                        "ok", true,
                        "title", "Successfully added.",
                        "text", fileForm.getFileCreate().getOriginalFilename() + " has been added."
                ));
    }

    private ResponseEntity<Resource> getFileForResponse(long id, String action, RedirectAttributes redirectAttributes) {
        Tika tika = new Tika();
        try {
            File file = fileStorage.getEntryContent(storageEntryRepository.findById(id).orElseThrow(
                    () -> new StorageException("Could not find entry", null)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(tika.detect(file)));
            headers.setContentDisposition(ContentDisposition
                    .builder(action)
                    .filename(file.getName(), StandardCharsets.UTF_8)
                    .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(file));

        } catch (StorageException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Could not identify file extension"
            );
        }
        return ResponseEntity
                .status(302)
                .header(HttpHeaders.LOCATION, "/not/found")
                .build();
    }
}

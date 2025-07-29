package org.example.notearchive.controller;

import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.example.notearchive.domain.Note;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.service.EntityHelper;
import org.example.notearchive.service.StorageEntryService;
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
    private final StorageEntryService storageService;
    private final CreateDirectoryValidator createDirectoryValidator;
    private final CreateFileValidator createFileValidator;
    private final FileStorage fileStorage;
    private final EntityHelper entityHelper;
    private final ResponseHelper responseHelper;

    public FileController(
            StorageEntryService storageService,
            CreateDirectoryValidator createDirectoryValidator,
            CreateFileValidator createFileValidator,
            FileStorage fileStorage,
            EntityHelper entityHelper,
            ResponseHelper responseHelper) {
        this.storageService = storageService;
        this.createDirectoryValidator = createDirectoryValidator;
        this.createFileValidator = createFileValidator;
        this.fileStorage = fileStorage;
        this.entityHelper = entityHelper;
        this.responseHelper = responseHelper;
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
            fileStorage.deleteEntry(entityHelper.getEntry(entryId));
        } catch (StorageException e) {
            return responseHelper.error("Could not delete " + entryName);
        }
        return responseHelper.ok("Successfully deleted.", entryName + " has been deleted.");
    }

    @PostMapping("/delete/note")
    @PreAuthorize("@userService.isNoteAuthor(#noteId, authentication)")
    public ResponseEntity<Map<String, Object>> deleteNote(@RequestParam("noteId") long noteId) {
        Note note = entityHelper.getNote(noteId);
        try {
            fileStorage.deleteNote(note);
        } catch (StorageException ignored) {
            return responseHelper.error("Could not delete note.");
        }
        return responseHelper.ok("Successfully deleted.", note.getTitle() + " has been deleted.");
    }

    @PostMapping("/folder/create")
    @PreAuthorize("@userService.canChangeEntry(#createDirectoryForm.parentId, authentication)")
    public ResponseEntity<Map<String, Object>> createDirectory(
            @Valid @ModelAttribute CreateDirectoryForm createDirectoryForm,
            BindingResult bindingResult
    ) {
        createDirectoryValidator.validate(createDirectoryForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return responseHelper.error(bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            fileStorage.createDirectory(
                    createDirectoryForm.getDirectoryName(),
                    entityHelper.getEntry(createDirectoryForm.getParentId())
            );
        } catch (StorageException ignored) {
            return responseHelper.error("Could not create " + createDirectoryForm.getDirectoryName() + ".");
        }
        return responseHelper.ok(
                "Successfully created.",
                createDirectoryForm.getDirectoryName() + " has been created."
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
            return responseHelper.error(bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            fileStorage.saveAsFile(fileForm.getFileCreate(), entityHelper.getEntry(fileForm.getFileParentId()));
        } catch (StorageException ignored) {
            return responseHelper.error("Could not create " + fileForm.getFileCreate().getOriginalFilename() + ".");
        }
        return responseHelper.ok(
                "Successfully added.",
                fileForm.getFileCreate().getOriginalFilename() + " has been added."
        );
    }

    private ResponseEntity<Resource> getFileForResponse(
            long id,
            String dispositionType,
            RedirectAttributes redirectAttributes
    ) {
        Tika tika = new Tika();
        try {
            File file = fileStorage.getEntryContent(entityHelper.getEntry(id));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(tika.detect(file)));
            headers.setContentDisposition(ContentDisposition
                    .builder(dispositionType)
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

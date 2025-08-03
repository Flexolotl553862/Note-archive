package org.example.notearchive.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.service.StorageEntryService;
import org.example.notearchive.validator.CreateDirectoryValidator;
import org.example.notearchive.validator.CreateFileValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FileController {
    private final CreateDirectoryValidator createDirectoryValidator;
    private final CreateFileValidator createFileValidator;
    private final FileStorage fileStorage;
    private final ResponseHelper responseHelper;
    private final StorageEntryService storageEntryService;

    @ModelAttribute
    public void initForms(Model model, Authentication authentication) {
        model.addAttribute("createDirectoryForm", new CreateDirectoryForm());
        model.addAttribute("fileForm", new FileForm());
        if (authentication != null) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/entry/{entry}")
    @PreAuthorize("@storageEntryService.canOpenEntry(#entry, authentication)")
    public ResponseEntity<Resource> openEntry(@PathVariable StorageEntry entry, RedirectAttributes redirectAttributes) {
        return fileStorage.getEntryContentForResponse(entry, "inline", redirectAttributes);
    }

    @GetMapping("/download/entry/{entry}")
    @PreAuthorize("@storageEntryService.canOpenEntry(#entry, authentication)")
    public ResponseEntity<Resource> downloadEntry(@PathVariable StorageEntry entry, RedirectAttributes redirectAttributes) {
        return fileStorage.getEntryContentForResponse(entry, "attachment", redirectAttributes);
    }

    @PostMapping("/delete/entry")
    @PreAuthorize("@storageEntryService.canChangeEntry(#entry, authentication)")
    public ResponseEntity<Map<String, Object>> deleteEntry(@RequestParam("entryId") StorageEntry entry) {
        try {
            fileStorage.deleteEntryContent(entry);
            storageEntryService.deleteEntry(entry);
        } catch (StorageException e) {
            return responseHelper.error("Could not delete " + entry.getName());
        }
        return responseHelper.ok("Successfully deleted.", entry.getName() + " has been deleted.");
    }

    @PostMapping("/folder/create")
    @PreAuthorize("@storageEntryService.canChangeEntry(#createDirectoryForm.parent, authentication)")
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
                    createDirectoryForm.getParent()
            );
            storageEntryService.createEntry(
                    createDirectoryForm.getParent(),
                    createDirectoryForm.getDirectoryName(),
                    StorageEntry.ENTRY_TYPE.DIRECTORY
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
    @PreAuthorize("@storageEntryService.canChangeEntry(#fileForm.parent, authentication)")
    public ResponseEntity<Map<String, Object>> createFile(
            @Valid @ModelAttribute FileForm fileForm,
            BindingResult bindingResult
    ) {
        createFileValidator.validate(fileForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return responseHelper.error(bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            fileStorage.saveAsFile(fileForm.getFileCreate(), fileForm.getParent());
            storageEntryService.createEntry(
                    fileForm.getParent(),
                    fileForm.getFileCreate().getOriginalFilename(),
                    StorageEntry.ENTRY_TYPE.FILE
            );
        } catch (StorageException ignored) {
            return responseHelper.error("Could not create " + fileForm.getFileCreate().getOriginalFilename() + ".");
        }
        return responseHelper.ok(
                "Successfully added.",
                fileForm.getFileCreate().getOriginalFilename() + " has been added."
        );
    }
}

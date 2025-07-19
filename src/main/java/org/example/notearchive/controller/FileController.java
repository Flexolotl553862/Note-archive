package org.example.notearchive.controller;

import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.service.StorageService;
import org.example.notearchive.validator.CreateDirectoryValidator;
import org.example.notearchive.validator.CreateFileValidator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

@Controller
public class FileController {
    private final StorageService storageService;
    private final CreateDirectoryValidator createDirectoryValidator;
    private final CreateFileValidator createFileValidator;

    public FileController(
            StorageService storageService,
            CreateDirectoryValidator createDirectoryValidator,
            CreateFileValidator createFileValidator) {
        this.storageService = storageService;
        this.createDirectoryValidator = createDirectoryValidator;
        this.createFileValidator = createFileValidator;
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
    @PreAuthorize("@userService.canChangeEntry(#id, authentication)")
    public String deleteEntry(@RequestParam("id") long id, @RequestParam("name") String name, Model model) {
        try {
            storageService.deleteEntryById(id);
        } catch (StorageException e) {
            return setError( "Could not delete " + name, model);
        }
        return setOk("Successfully deleted.", name + " has been deleted.", model);
    }

    @PostMapping("/folder/create")
    @PreAuthorize("@userService.canChangeEntry(#createDirectoryForm.parentId, authentication)")
    public String createDirectory(
            @Valid @ModelAttribute CreateDirectoryForm createDirectoryForm,
            BindingResult bindingResult,
            Model model) {

        createDirectoryValidator.validate(createDirectoryForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return setError(bindingResult.getFieldError().getDefaultMessage(), model);
        }
        try {
            storageService.createDirectory(
                    createDirectoryForm.getDirectoryName(),
                    createDirectoryForm.getParentId()
            );
        } catch (StorageException ignored) {
            return setError("Could not create " + createDirectoryForm.getDirectoryName() + ".", model);
        }
        return setOk(
                "Successfully created.",
                createDirectoryForm.getDirectoryName() + " has been created.",
                model
        );
    }

    @PostMapping("/file/create")
    @PreAuthorize("@userService.canChangeEntry(#fileForm.fileParentId, authentication)")
    public String createFile(
            @Valid @ModelAttribute FileForm fileForm,
            BindingResult bindingResult,
            Model model
    ) {
        createFileValidator.validate(fileForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return setError(bindingResult.getFieldError().getDefaultMessage(), model);
        }
        try {
            storageService.uploadMultipartFile(fileForm.getFileCreate(), fileForm.getFileParentId());
        } catch (StorageException ignored) {
            return setError("Could not create " + fileForm.getFileCreate().getOriginalFilename() + ".", model);
        }
        return setOk(
                "Successfully added.",
                fileForm.getFileCreate().getOriginalFilename() + " has been added.",
                model
        );
    }

    private String setOk(String title, String message, Model model) {
        model.addAttribute("title", title);
        model.addAttribute("message", message);
        return "/fragments/success-notification";
    }

    private String setError(String message, Model model) {
        model.addAttribute("title", "Error!");
        model.addAttribute("message", message);
        return "/fragments/error-notification";
    }

    private ResponseEntity<Resource> getFileForResponse(long id, String action, RedirectAttributes redirectAttributes) {
        Tika tika = new Tika();
        try {
            File file = storageService.getEntryContent(id);
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            action + ";filename=\"" + file.getName() + "\""
                    )
                    .contentType(MediaType.parseMediaType(tika.detect(file)))
                    .body(resource);
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

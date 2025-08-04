package org.example.notearchive.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.dto.RenewLinkForm;
import org.example.notearchive.exception.MyLinkException;
import org.example.notearchive.service.LinkService;
import org.example.notearchive.validator.LinkValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LinkPageController {
    private final LinkService linkService;
    private final LinkValidator linkValidator;
    private final ResponseHelper responseHelper;

    @PostMapping("/generate/link")
    @PreAuthorize("@noteService.isNoteEditor(#linkForm.note, authentication)")
    public ResponseEntity<Map<String, Object>> generateLink(
            @Valid @ModelAttribute LinkForm linkForm,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        linkValidator.validate(linkForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return responseHelper.error(bindingResult.getFieldError().getDefaultMessage());
        }
        try {
            linkService.generateLink(linkForm, authentication);
        } catch (MyLinkException e) {
            return responseHelper.error("Link generation failed");
        }
        return responseHelper.ok("Successfully created", "Link has been created!");
    }

    @PostMapping("/delete/link")
    @PreAuthorize("@myLinkService.isLinkAuthor(#link, authentication)")
    public ResponseEntity<Map<String, Object>> deleteLink(@RequestParam Link link) {
        linkService.deleteLink(link);
        return responseHelper.ok("Successfully deleted", "Link has been deleted!");
    }

    @PostMapping("/renew/link")
    @PreAuthorize("@myLinkService.isLinkAuthor(#renewLinkForm.link, authentication)")
    public ResponseEntity<Map<String, Object>> renewLink(@ModelAttribute RenewLinkForm renewLinkForm) {
        try {
            linkService.renewLink(renewLinkForm.getLink(), renewLinkForm.getDate());
        } catch (MyLinkException e) {
            return responseHelper.error(e.getMessage());
        }
        return responseHelper.ok("Successfully renewed", "Link has been renewed!");
    }

    @GetMapping("/note/{note}/links")
    @PreAuthorize("@noteService.isNoteEditor(#note, authentication)")
    public String getNoteLinks(
            @PathVariable Note note,
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        model.addAllAttributes(Map.of(
                "links", note.getLinks().stream()
                        .filter(link -> link.getAuthor().equals(authentication.getPrincipal())).toList(),
                "linkForm", new LinkForm(),
                "renewLinkForm", new RenewLinkForm(),
                "noteId", note.getId(),
                "root", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
        ));
        return "note-links";
    }
}

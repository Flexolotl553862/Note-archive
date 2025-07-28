package org.example.notearchive.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.notearchive.domain.Link;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.dto.RenewLinkForm;
import org.example.notearchive.service.EntityHelper;
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
public class LinkController {
    private final LinkService linkService;
    private final LinkValidator linkValidator;
    private final ResponseHelper responseHelper;
    private final EntityHelper entityHelper;

    public LinkController(
            LinkService linkService,
            LinkValidator linkValidator,
            ResponseHelper responseHelper,
            EntityHelper entityHelper
    ) {
        this.linkService = linkService;
        this.linkValidator = linkValidator;
        this.responseHelper = responseHelper;
        this.entityHelper = entityHelper;
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
            return responseHelper.error(bindingResult.getFieldError().getDefaultMessage());
        }
        linkService.generateLink(linkForm, authentication);
        return responseHelper.ok("Successfully created", "Link has been created!");
    }

    @PostMapping("/delete/link")
    @PreAuthorize("@userService.isLinkAuthor(#linkId, authentication)")
    public ResponseEntity<Map<String, Object>> deleteLink(
            @RequestParam("linkId") Long linkId
    ) {
        linkService.deleteLink(entityHelper.getLink(linkId));
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
        Link link = entityHelper.getLink(renewLinkForm.getLinkId());
        if (!linkService.isLinkActive(link)) {
            return responseHelper.error("Could not set date in past.");
        }
        linkService.renewLink(link, renewLinkForm.getDate());
        return responseHelper.ok("Successfully renewed", "Link has been renewed!");
    }

    @GetMapping("/note/{id}/links")
    @PreAuthorize("@userService.isNoteEditor(#noteId, authentication)")
    public String getNoteLinks(
            @PathVariable("id") long noteId,
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        model.addAllAttributes(Map.of(
                "links", entityHelper
                        .getNote(noteId)
                        .getLinks()
                        .stream()
                        .filter(link -> link.getAuthor().equals(authentication.getPrincipal()))
                        .toList(),
                "linkForm", new LinkForm(),
                "renewLinkForm", new RenewLinkForm(),
                "noteId", noteId,
                "root", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
        ));
        return "note-links";
    }
}

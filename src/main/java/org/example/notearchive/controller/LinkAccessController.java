package org.example.notearchive.controller;

import org.example.notearchive.service.LinkService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public/{slug}")
public class LinkAccessController {
    private final LinkService linkService;
    private final FileController fileController;
    private final NotePageController notePageController;

    public LinkAccessController(
            LinkService linkService,
            FileController fileController,
            NotePageController notePageController
    ) {
        this.linkService = linkService;
        this.fileController = fileController;
        this.notePageController = notePageController;
    }

    @ModelAttribute
    public void initAll(@PathVariable String slug, Model model) {
        model.addAttribute("prefix", "/public/" + slug);
    }

    @GetMapping("/note/{id}")
    public String getNote(@PathVariable(value = "slug", required = false) String slug, @PathVariable Long id) {
        if (!linkService.canOpenNote(slug, id)) {
            return "redirect:/not/found";
        }
        return "redirect:/public/" + slug + "/note/" + id + "/description";
    }

    @GetMapping("/folder/{id}")
    public String getFile(
            @PathVariable(value = "slug", required = false) String slug,
            @PathVariable Long id,
            Model model
    ) {
        if (!linkService.canOpenEntry(slug, id)) {
            return "redirect:/not/found";
        }
        return notePageController.folder(id, model);
    }

    @GetMapping("/note/{id}/description")
    public String noteDescription(@PathVariable("slug") String slug, @PathVariable("id") long id, Model model) {
        if (!linkService.canOpenNote(slug, id)) {
            return "redirect:/not/found";
        }
        return notePageController.noteDescription(id, model);
    }

    @GetMapping("/api/note/{id}/description")
    public ResponseEntity<String> getMarkdownDescription(@PathVariable("slug") String slug, @PathVariable long id) {
        if (!linkService.canOpenNote(slug, id)) {
            return ResponseEntity.notFound().build();
        }
        return notePageController.getMarkdownDescription(id);
    }

    @GetMapping("/download/file/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable long id,
            @PathVariable("slug") String slug,
            RedirectAttributes redirectAttributes
    ) {
        if (!linkService.canOpenEntry(slug, id)) {
            return ResponseEntity.notFound().build();
        }
        return fileController.downloadFile(id, redirectAttributes);
    }
}

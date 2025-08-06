package org.example.notearchive.controller;

import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.service.LinkService;
import org.example.notearchive.service.NoteService;
import org.example.notearchive.service.StorageEntryService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/public/{slug}")
@RequiredArgsConstructor
public class LinkAccessController {
    private final StorageEntryService storageEntryService;
    private final LinkService myLinkService;
    private final NoteService noteService;
    private final FileStorage fileStorage;

    @ModelAttribute
    public void initAll(@PathVariable("slug") String slug, Model model) {
        model.addAttribute("prefix", "/public/" + slug);
        model.addAttribute("createDirectoryForm", new CreateDirectoryForm());
        model.addAttribute("fileForm", new FileForm());
    }

    @GetMapping("/note/{note}")
    @PreAuthorize("@myLinkService.canOpenNoteByLink(#note, @myLinkService.getLinkByLink(#slug))")
    public String getNote(@PathVariable(value = "slug") String slug, @PathVariable Note note) {
        return "redirect:/public/" + slug + "/note/" + note.getId() + "/description";
    }

    @GetMapping("/folder/{folder}")
    @PreAuthorize("@myLinkService.canOpenEntryByLink(#folder, @myLinkService.getLinkByLink(#slug))")
    public String folder(
            @PathVariable("slug") String slug,
            @PathVariable StorageEntry folder,
            Model model
    ) {
        if (!folder.isDirectory()) {
            return "redirect:/not/found";
        }
        model.addAllAttributes(Map.of(
                "path", folder.getPathAsEntryList(),
                "folder", folder,
                "note", folder.getParentNote(),
                "children", storageEntryService.getAccessibleChildren(folder, myLinkService.getLinkByLink(slug))
        ));
        return "note";
    }

    @GetMapping("/entry/{entry}")
    @PreAuthorize("@myLinkService.canOpenEntryByLink(#entry, @myLinkService.getLinkByLink(#slug))")
    public ResponseEntity<Resource> openEntry(
            @PathVariable(value = "slug") String slug,
            @PathVariable StorageEntry entry,
            RedirectAttributes redirectAttributes
    ) {
        return fileStorage.getEntryContentForResponse(entry, "inline", redirectAttributes);
    }

    @GetMapping("/download/entry/{entry}")
    @PreAuthorize("@myLinkService.canOpenEntryByLink(#entry, @myLinkService.getLinkByLink(#slug))")
    public ResponseEntity<Resource> downloadEntry(
            @PathVariable(value = "slug") String slug,
            @PathVariable StorageEntry entry,
            RedirectAttributes redirectAttributes
    ) {
        return fileStorage.getEntryContentForResponse(entry, "attachment", redirectAttributes);
    }

    @GetMapping("/note/{note}/description")
    @PreAuthorize("@myLinkService.canOpenNoteByLink(#note, @myLinkService.getLinkByLink(#slug))")
    public String noteDescription(
            @PathVariable(value = "slug") String slug,
            @PathVariable Note note,
            Model model
    ) {
        model.addAttribute("note", note);
        return "note-description";
    }

    @GetMapping("/api/note/{note}/description")
    @PreAuthorize("@myLinkService.canOpenNoteByLink(#note, @myLinkService.getLinkByLink(#slug))")
    public ResponseEntity<String> getMarkdownDescription(
            @PathVariable(value = "slug") String slug,
            @PathVariable Note note
    ) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body(noteService.getMarkdownDescription(note));
        } catch (Exception ignored) {}
        return ResponseEntity.notFound().build();
    }
}

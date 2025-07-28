package org.example.notearchive.service;

import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.repository.LinkRepository;
import org.example.notearchive.repository.NoteRepository;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service("myLinkService")
public class LinkService {
    private final LinkRepository linkRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final NoteRepository noteRepository;

    public LinkService(LinkRepository linkRepository, StorageEntryRepository storageEntryRepository, NoteRepository noteRepository) {
        this.linkRepository = linkRepository;
        this.storageEntryRepository = storageEntryRepository;
        this.noteRepository = noteRepository;
    }

    public boolean canOpenNote(String slug, Note note) {
        Link link = linkRepository.findByLink(slug).orElse(null);
        return (link != null
                && !link.getExpiryDate().before(new Date())
                && note.getEditors().contains(link.getAuthor()));
    }

    public boolean canOpenNote(String slug, Long id) {
        Note note = noteRepository.findById(id).orElse(null);
        return note != null && canOpenNote(slug, note);
    }

    public boolean canOpenEntry(String slug, Long id) {
        StorageEntry entry = storageEntryRepository.findById(id).orElse(null);
        if (entry == null) {
            return false;
        }
        return canOpenNote(slug, entry.getParentNote());
    }

    public void generateLink(LinkForm linkForm, Authentication authentication) {
        Link link = new Link(
                UUID.randomUUID().toString(),
                linkForm.getDescription(),
                noteRepository.findById(linkForm.getNoteId()).orElseThrow(),
                linkForm.getDate(),
                (User) authentication.getPrincipal()
        );
        linkRepository.save(link);
    }

    public void deleteLink(Link link) {
        linkRepository.delete(link);
    }

    public boolean isLinkActive(Link link) {
        return link.getExpiryDate().after(new Date());
    }

    public void renewLink(Link link, Date date) {
        link.setExpiryDate(date);
        linkRepository.save(link);
    }
}

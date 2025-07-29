package org.example.notearchive.service;

import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.repository.LinkRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service("myLinkService")
public class LinkService {
    private final LinkRepository linkRepository;
    private final EntityHelper entityHelper;

    public LinkService(LinkRepository linkRepository, EntityHelper entityHelper) {
        this.linkRepository = linkRepository;
        this.entityHelper = entityHelper;
    }

    public boolean canOpenNote(String slug, Note note) {
        Link link = linkRepository.findByLink(slug).orElse(null);
        return (link != null
                && !link.getExpiryDate().before(new Date())
                && note.getEditors().contains(link.getAuthor()));
    }

    public boolean canOpenEntry(String slug, StorageEntry entry) {
        return canOpenNote(slug, entry.getParentNote());
    }

    public void generateLink(LinkForm linkForm, Authentication authentication) {
        Link link = new Link(
                UUID.randomUUID().toString(),
                linkForm.getDescription(),
                entityHelper.getNote(linkForm.getNoteId()),
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

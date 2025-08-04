package org.example.notearchive.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.LinkForm;
import org.example.notearchive.exception.MyLinkException;
import org.example.notearchive.repository.LinkRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service("myLinkService")
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;

    public boolean isLinkAuthor(Link link, Authentication authentication) {
        return link.getAuthor().getId().equals(
                ((User) authentication.getPrincipal()).getId()
        );
    }

    public boolean linkIsActive(Link link) {
        return link.getExpiryDate().after(new Date());
    }

    public boolean canOpenNoteByLink(Note note, Link link) {
        return linkIsActive(link) && link.getNote().equals(note);
    }

    public boolean canOpenEntryByLink(StorageEntry entry, Link link) {
        return canOpenNoteByLink(entry.getParentNote(), link) && !entry.getLock();
    }

    public void generateLink(LinkForm linkForm, Authentication authentication) throws MyLinkException {
        int attempts = 10;
        while(attempts > 0) {
            try {
                String slug = UUID.randomUUID().toString();
                Link link = new Link(
                        slug,
                        linkForm.getDescription(),
                        linkForm.getNote(),
                        linkForm.getDate(),
                        (User) authentication.getPrincipal()
                );
                linkRepository.save(link);
                attempts = 0;
            } catch (DataIntegrityViolationException ignored) {
                attempts--;
            }
        }
        if (attempts != 0) {
            throw new MyLinkException("Could not generate new slug after 10 attempts", null);
        }
    }

    public void deleteLink(Link link) {
        linkRepository.delete(link);
    }

    public void renewLink(Link link, Date date) throws MyLinkException {
        if (!linkIsActive(link)) {
            throw new MyLinkException("Link is expired", null);
        }
        link.setExpiryDate(date);
        linkRepository.save(link);
    }

    public Link getLinkByLink(String link) {
        return linkRepository.findByLink(link).orElseThrow(
                () -> new EntityNotFoundException("No such link")
        );
    }
}
